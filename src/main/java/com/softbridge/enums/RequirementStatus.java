package com.softbridge.enums;

/**
 * Lifecycle status of a submitted software requirement.
 */
public enum RequirementStatus {
    PENDING,    // submitted, awaiting admin decision
    INHOUSE,    // admin decided to build internally
    OUTSOURCE   // admin decided to route to external partner
}
