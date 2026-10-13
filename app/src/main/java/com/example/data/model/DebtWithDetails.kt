package com.example.data.model

data class DebtWithDetails(
    val debt: DebtEntity,
    val events: List<DebtEventEntity> = emptyList(),
    val linkedAccountName: String? = null
) {
    val totalPrincipalPaisa: Long
        get() {
            val advances = events.filter { it.type == DebtEventType.ADDITIONAL_ADVANCE }.sumOf { it.amountPaisa }
            return debt.originalPrincipalPaisa + advances
        }

    val totalRepaidPaisa: Long
        get() = events.filter { it.type == DebtEventType.REPAYMENT }.sumOf { it.amountPaisa }

    val totalAdjustmentsPaisa: Long
        get() = events.filter { it.type == DebtEventType.ADJUSTMENT }.sumOf { it.amountPaisa }

    val outstandingPaisa: Long
        get() {
            if (debt.status == DebtStatus.SETTLED) return 0L
            if (debt.status == DebtStatus.WRITTEN_OFF) return 0L
            val raw = totalPrincipalPaisa - totalRepaidPaisa + totalAdjustmentsPaisa
            return raw.coerceAtLeast(0L)
        }

    val isOverdue: Boolean
        get() {
            if (debt.status == DebtStatus.SETTLED || debt.status == DebtStatus.WRITTEN_OFF) return false
            val due = debt.dueDateEpochMs ?: return false
            return due < System.currentTimeMillis() && outstandingPaisa > 0L
        }

    val isDueSoon: Boolean
        get() {
            if (debt.status == DebtStatus.SETTLED || debt.status == DebtStatus.WRITTEN_OFF) return false
            val due = debt.dueDateEpochMs ?: return false
            val now = System.currentTimeMillis()
            val sevenDaysMs = 7 * 24 * 60 * 60 * 1000L
            return due >= now && due <= (now + sevenDaysMs) && outstandingPaisa > 0L
        }

    val calculatedStatus: DebtStatus
        get() {
            if (debt.status == DebtStatus.WRITTEN_OFF) return DebtStatus.WRITTEN_OFF
            if (outstandingPaisa == 0L && totalRepaidPaisa > 0L) return DebtStatus.SETTLED
            if (totalRepaidPaisa > 0L && outstandingPaisa > 0L) return DebtStatus.PARTIALLY_PAID
            return debt.status
        }
}

data class KarzSummary(
    val totalGavePaisa: Long,      // Money owed to me (I_GAVE)
    val totalTookPaisa: Long,      // Money I owe others (I_TOOK)
    val dueSoonCount: Int,
    val dueSoonPaisa: Long,
    val overdueCount: Int,
    val overduePaisa: Long
) {
    val netKarzPaisa: Long get() = totalGavePaisa - totalTookPaisa
}
