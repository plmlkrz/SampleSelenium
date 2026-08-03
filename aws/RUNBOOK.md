# Drill 13 Runbook — stand up a real AWS endpoint, then test it

Goal: about 45 minutes of clicking, after which "I've deployed a Lambda that writes to S3
and wrote an automated suite against it" is a **true sentence with a concrete noun behind it**.
Nothing here is impressive architecture. That is fine — the point is that it is real, it is
yours, and you tested it. That is the part interviewers can tell you did.

You do steps 1 and 2 yourself: creating the account and entering the card are yours to do,
not something to hand to an assistant.

---

## 0. What you are building

```
  Java test suite (drills/d13_aws)          AWS
  REST Assured over HTTPS  ────────►  Lambda Function URL
                                            │  (Python 3.13, ~90 lines)
                                            ▼
                                        S3 bucket   notes/<uuid>.json
                                            │
                                            ▼
                                      CloudWatch Logs
```

Files in this folder:

| File | What to do with it |
|---|---|
| `lambda_function.py` | Paste into the Lambda console code editor, click **Deploy** |
| `iam-policy.json` | Paste as an inline policy on the function's execution role |
| `RUNBOOK.md` | This file |

---

## 1. Create the free-tier account (you, ~15 min)

<https://portal.aws.amazon.com/billing/signup> — needs an email, a phone number, and a
credit/debit card. The card is for identity verification and overage; everything in this
drill sits inside the perpetual free tier (1M Lambda requests/month, 5 GB S3). Expect a
$1 temporary authorization that drops off.

Pick a region and **use the same one for every step** — `us-east-1` (N. Virginia) is the
default and the one every tutorial assumes. A bucket in one region and a Lambda in another
is the single most common way this goes wrong.

## 2. Set a budget alarm BEFORE you build anything (you, ~3 min)

Billing and Cost Management → **Budgets** → Create budget → Use a template → **Zero spend
budget** → your email → Create. Now anything that starts costing money emails you the same day.

Do this first. It is also a genuinely good answer when someone asks how you'd keep a test
environment from surprising the client with a bill.

## 3. S3 bucket (~2 min)

S3 → Create bucket.
- Name: globally unique across all of AWS — e.g. `pk-notes-drill-<4 random digits>`
- Region: the one you picked
- **Block all public access: leave CHECKED.** The Lambda reaches the bucket through IAM,
  not through public URLs. A public bucket is the classic breach headline; do not create one.
- Everything else default → Create bucket. Write the name down.

## 4. Lambda function (~10 min)

Lambda → Create function → **Author from scratch**
- Name: `note-store`
- Runtime: **Python 3.13**
- Architecture: arm64 (cheaper; irrelevant at free tier, but it is the current default)
- Permissions: leave "Create a new role with basic Lambda permissions" → Create function

Then, on the function page:

**a. Code.** Open `lambda_function.py` from this folder, copy the whole thing, select-all in
the console editor and paste over it. Click **Deploy**. (Green "changes deployed" banner.
Deploying is not optional — the editor happily runs stale code otherwise.)

**b. Environment variables.** Configuration → Environment variables → Edit → Add:

| Key | Value |
|---|---|
| `BUCKET_NAME` | your bucket name from step 3 |
| `API_KEY` | a long random string — generate one below |

```bash
python -c "import secrets; print(secrets.token_urlsafe(32))"
```

**c. Timeout.** Configuration → General configuration → Edit → Timeout **10 sec** (the 3 sec
default is tight for a cold start plus an S3 round trip). Save.

**d. S3 permission.** Configuration → Permissions → click the **execution role** name (opens
IAM in a new tab) → Add permissions → Create inline policy → **JSON** tab → paste
`iam-policy.json`, replacing `REPLACE-ME-BUCKET-NAME` with your bucket name → Next → name it
`note-store-s3` → Create policy.

This is least privilege, and worth being able to explain: the function can `PutObject` and
`GetObject` under the `notes/` prefix of exactly one bucket. It cannot list the bucket, cannot
delete, cannot touch any other bucket. If someone asks "how do you know the blast radius of a
compromised function?", this policy is the answer.

**e. Function URL.** Back on the Lambda tab: Configuration → Function URL → Create function URL
- Auth type: **NONE** (the function checks `x-api-key` itself)
- CORS: leave off
- Create. Copy the URL — `https://<hash>.lambda-url.<region>.on.aws/`

> Auth NONE means anyone with the hostname can invoke it. That is why the budget alarm is
> step 2, why the key is random, and why step 8 deletes it. Do not put anything real in a note.

## 5. Smoke test by hand (~2 min)

```bash
curl -s https://YOUR-URL.lambda-url.us-east-1.on.aws/
```

Expect `{"service": "note-store", "status": "ok", ...}`.

```bash
curl -s -i -X POST https://YOUR-URL.lambda-url.us-east-1.on.aws/notes -H "x-api-key: YOUR_KEY" -H "content-type: application/json" -d '{"text":"first note"}'
```

Expect `HTTP/1.1 201`, a `location: /notes/<uuid>` header, and a JSON body. Then look in the
S3 console — there is a `notes/` folder with your object in it. That is the moment the sentence
becomes true.

**When it does not work,** go to the Lambda page → Monitor → **View CloudWatch logs** → newest
log stream. The Python traceback is there. The three usual causes:
- `KeyError: 'BUCKET_NAME'` — env var not saved (step 4b)
- `AccessDenied` on PutObject — inline policy missing, or the bucket name in it does not match
- `403 Forbidden` from the URL itself with no log line at all — the request never reached your
  code; re-check the Function URL auth type

Reading the log to find out which of those it is *is* the drill. That triage loop —
"is it the client, the permission, or the code?" — is the actual interview content.

## 6. Point the Java suite at it (~1 min)

```powershell
$env:AWS_FN_URL='https://YOUR-URL.lambda-url.us-east-1.on.aws'
$env:AWS_API_KEY='YOUR_KEY'
mvn test -Dtest=SourceD13AwsDrills
```

No trailing slash on `AWS_FN_URL`. Without those two variables the suite **skips** rather than
fails, so `mvn test` still passes on a clean checkout — same convention as the Playwright module.

## 7. Do the drill

`src/test/java/com/sampleselenium/drills/d13_aws/` — read `SourceD13AwsDrills.java`, then close
it and rebuild it from memory in `PracticeD13AwsDrills.java`, same loop as every other module.

## 8. Tear it down when you are done (~3 min)

Leaving it up costs nothing at free tier, but leaving a keyed public endpoint running for
months is not a habit worth building. When you no longer need it live:

1. Lambda → Configuration → Function URL → **Delete** (kills public access immediately —
   do this one first even if you keep everything else)
2. Lambda → Actions → Delete function
3. S3 → select bucket → Empty, then Delete
4. IAM → Roles → delete `note-store-role-*`
5. CloudWatch → Log groups → delete `/aws/lambda/note-store`

Screenshot the working `curl` output and the S3 object list before you delete. That is your
evidence, and re-deploying from this folder takes 15 minutes if you need it live again.

---

## What you can honestly say afterwards

Only claim what actually happened. After this drill, all of these are true:

- "I stood up a Lambda behind a function URL that persists to S3, and wrote a REST Assured
  suite against the deployed endpoint — happy path, 401 on a bad key, 400 on invalid input,
  404 on a missing object."
- "I scoped its IAM execution role to GetObject/PutObject on one prefix of one bucket, so I
  can tell you exactly what a compromised function could reach."
- "When it broke, I triaged it in CloudWatch Logs — the failure was a permissions error on
  PutObject, not application code." *(only if that is what happened to you — if it was the
  env var, say the env var)*
- "It is a small thing I built to get hands-on rather than talk about AWS from documentation."

What is **not** true and should not be implied: production traffic, scale, cost optimization,
multi-account setups, or IaC. If asked about Terraform or CloudFormation, the honest answer is
that you clicked this one through the console deliberately to see each piece, and that the next
step is expressing it as code. Saying that is stronger than bluffing — anyone who has run a
pipeline can tell in two follow-up questions.
