package com.healthcare.inventory.enums;

/**
 * Direction and intent of an inventory movement.
 *
 *  PURCHASE  — stock received from supplier      (+)
 *  DISPENSE  — medicines dispensed to patient    (-)
 *  ADJUSTMENT— manual correction / write-off     (+/-)
 *  EXPIRED   — batch removed due to expiry       (-)
 *  RETURNED  — patient return or supplier return (+)
 */
public enum TransactionType {
    PURCHASE,
    DISPENSE,
    ADJUSTMENT,
    EXPIRED,
    RETURNED
}
