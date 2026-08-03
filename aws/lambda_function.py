"""
Paste this whole file into the AWS Lambda console code editor (runtime: Python 3.13),
click Deploy, and you have a running service. No build tools, no zip, no CLI.

WHAT IT IS
  A tiny "notes" API behind a Lambda Function URL, storing each note as a JSON object
  in S3. It is deliberately shaped like the mock API in drill 09 so the Java tests in
  drills/d13_aws can assert the same things (200 vs 201, Location header, 401, 404,
  400) — except this time against a real deployed endpoint over the public internet.

ROUTES (Function URL payload format 2.0)
  GET  /                -> 200  health JSON
  POST /notes           -> 201  + Location header; body {"text": "...'} written to S3
                           400  if text is missing/blank/too long
                           401  if x-api-key header is absent or wrong
  GET  /notes/{id}      -> 200  the stored note read back out of S3
                           404  if that key does not exist
  anything else         -> 404

CONFIG — set these as Lambda environment variables (Configuration > Environment variables):
  BUCKET_NAME   the S3 bucket you created
  API_KEY       a long random string you generate; the tests send it as x-api-key

WHY AN API KEY ON A PUBLIC URL
  A Function URL with auth type NONE is reachable by anyone who learns the hostname.
  The key is not real security (it is a shared secret in plaintext), it is a speed bump
  plus an honest 401 case to test. Keep the bucket private, keep the note payload
  boring, and delete the whole stack when the drill is done — see RUNBOOK.md.
"""

import json
import os
import uuid
from datetime import datetime, timezone

import boto3  # already present in the Lambda Python runtime — nothing to install
from botocore.exceptions import ClientError

s3 = boto3.client("s3")

BUCKET = os.environ["BUCKET_NAME"]
API_KEY = os.environ["API_KEY"]
MAX_TEXT_LEN = 500


def _response(status, body, extra_headers=None):
    headers = {"Content-Type": "application/json"}
    if extra_headers:
        headers.update(extra_headers)
    return {
        "statusCode": status,
        "headers": headers,
        "body": json.dumps(body),
    }


def _key_for(note_id):
    return f"notes/{note_id}.json"


def lambda_handler(event, context):
    # Function URLs use payload format 2.0: method and path live under requestContext.
    method = event.get("requestContext", {}).get("http", {}).get("method", "GET")
    path = event.get("rawPath", "/")
    # API Gateway/Function URL lower-cases every incoming header name for you.
    headers = event.get("headers") or {}

    if method == "GET" and path == "/":
        return _response(200, {
            "service": "note-store",
            "status": "ok",
            "time": datetime.now(timezone.utc).isoformat(),
        })

    if method == "POST" and path == "/notes":
        # 401 = "I don't know who you are." (403 would be "I know you, and no.")
        if headers.get("x-api-key") != API_KEY:
            return _response(401, {"error": "unauthenticated",
                                   "message": "missing or invalid x-api-key"})

        try:
            payload = json.loads(event.get("body") or "{}")
        except json.JSONDecodeError:
            return _response(400, {"error": "bad_request", "message": "body is not valid JSON"})

        text = payload.get("text")
        if not isinstance(text, str) or not text.strip():
            return _response(400, {"error": "bad_request", "message": "text is required"})
        if len(text) > MAX_TEXT_LEN:
            return _response(400, {"error": "bad_request",
                                   "message": f"text exceeds {MAX_TEXT_LEN} characters"})

        note_id = str(uuid.uuid4())
        note = {
            "id": note_id,
            "text": text.strip(),
            "createdAt": datetime.now(timezone.utc).isoformat(),
        }
        s3.put_object(
            Bucket=BUCKET,
            Key=_key_for(note_id),
            Body=json.dumps(note).encode("utf-8"),
            ContentType="application/json",
        )
        # 201 CREATED must carry a Location header pointing at the new resource.
        return _response(201, note, {"Location": f"/notes/{note_id}"})

    if method == "GET" and path.startswith("/notes/"):
        note_id = path[len("/notes/"):]
        if not note_id or "/" in note_id:
            return _response(404, {"error": "not_found"})
        try:
            obj = s3.get_object(Bucket=BUCKET, Key=_key_for(note_id))
        except ClientError as err:
            if err.response["Error"]["Code"] in ("NoSuchKey", "404"):
                return _response(404, {"error": "not_found",
                                       "message": f"no note with id {note_id}"})
            raise
        return _response(200, json.loads(obj["Body"].read()))

    return _response(404, {"error": "not_found", "message": f"no route for {method} {path}"})
