package com.hassiel.rideadvisor.utils

import android.content.Context
import com.hassiel.rideadvisor.R
import com.hassiel.rideadvisor.data.RejectionReason

/** Traduce un [RejectionReason] (o su nombre serializado) a texto legible. */
object RejectionReasonText {

    fun forReason(context: Context, reason: RejectionReason): String = when (reason) {
        RejectionReason.PRICE_TOO_LOW -> context.getString(R.string.rejection_price)
        RejectionReason.DISTANCE_TOO_HIGH -> context.getString(R.string.rejection_distance)
        RejectionReason.APP_NOT_ALLOWED -> context.getString(R.string.rejection_app)
        RejectionReason.INCOMPLETE_DATA -> context.getString(R.string.rejection_incomplete)
    }

    fun forName(context: Context, name: String?): String? {
        if (name == null) return null
        val reason = try {
            RejectionReason.valueOf(name)
        } catch (e: IllegalArgumentException) {
            return null
        }
        return forReason(context, reason)
    }
}
