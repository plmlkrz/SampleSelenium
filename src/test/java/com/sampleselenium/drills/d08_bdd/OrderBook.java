package com.sampleselenium.drills.d08_bdd;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * DRILL 08 — the tiny system under test for the browser-free BDD drills.
 *
 * There is no browser and no network here on purpose. Module 8's other drill
 * (d08_login.feature + LoginSteps) already proves you can wire Cucumber to
 * Selenium. This one strips the browser away so the drill is about the part
 * interviewers actually make you write on a whiteboard: feature, steps, runner,
 * hooks, Cucumber expressions, DataTable, and Scenario Outline.
 *
 * MONEY IS BigDecimal, NEVER double. Say this out loud if a panel asks about
 * validating financial calculations: doubles cannot represent 0.1 exactly, so
 * 0.1 + 0.2 != 0.3 and cent-level assertions fail intermittently. Use BigDecimal
 * with an explicit scale and RoundingMode, and compare with compareTo rather than
 * equals, because equals also compares scale (2.50 does not equal 2.5).
 */
public class OrderBook {

    /** Free shipping at or above this subtotal. The boundary is the interesting test. */
    public static final BigDecimal FREE_SHIPPING_THRESHOLD = new BigDecimal("50.00");
    public static final BigDecimal FLAT_SHIPPING = new BigDecimal("7.95");

    private final List<Line> lines = new ArrayList<>();
    private String discountCode = "";

    public void addLine(String item, int quantity, BigDecimal unitPrice) {
        lines.add(new Line(item, quantity, unitPrice));
    }

    public void applyDiscountCode(String code) {
        this.discountCode = code == null ? "" : code.trim().toUpperCase();
    }

    public BigDecimal subtotal() {
        return lines.stream()
                .map(line -> line.unitPrice().multiply(BigDecimal.valueOf(line.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /** SAVE10 takes 10% off the subtotal. Any other code is worth nothing. */
    public BigDecimal discount() {
        if (!"SAVE10".equals(discountCode)) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return subtotal().multiply(new BigDecimal("0.10")).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * THE RULE WORTH ARGUING ABOUT IN REFINEMENT: the free-shipping threshold is
     * checked against the DISCOUNTED total, not the subtotal. So a $52 order with
     * SAVE10 drops to $46.80 and starts paying shipping again. That is either the
     * intended rule or a defect, and the only way to know is to ask. A feature file
     * is where that ambiguity gets settled in writing before anyone builds it.
     */
    public BigDecimal shipping() {
        BigDecimal afterDiscount = subtotal().subtract(discount());
        return afterDiscount.compareTo(FREE_SHIPPING_THRESHOLD) >= 0
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : FLAT_SHIPPING;
    }

    public BigDecimal total() {
        return subtotal().subtract(discount()).add(shipping()).setScale(2, RoundingMode.HALF_UP);
    }

    public int itemCount() {
        return lines.stream().mapToInt(Line::quantity).sum();
    }

    /** A record is the right shape for a value holder: immutable, no boilerplate. */
    public record Line(String item, int quantity, BigDecimal unitPrice) {
    }
}
