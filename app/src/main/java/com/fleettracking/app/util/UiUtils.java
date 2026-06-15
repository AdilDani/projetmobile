package com.fleettracking.app.util;

import android.content.Context;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.fleettracking.app.R;

/** Maps domain status strings to colors / chip backgrounds. */
public final class UiUtils {

    private UiUtils() {}

    public static int statusColor(Context ctx, String statut) {
        int res;
        switch (statut) {
            case "Disponible":
            case "Résolu":
                res = R.color.success; break;
            case "Assigné":
                res = R.color.info; break;
            case "En trajet":
                res = R.color.primary; break;
            case "Indisponible":
                res = R.color.warning; break;
            case "En cours":
                res = R.color.danger; break;
            default:
                res = R.color.text_secondary;
        }
        return ContextCompat.getColor(ctx, res);
    }

    public static int statusChipBg(String statut) {
        switch (statut) {
            case "Disponible":
            case "Résolu":
                return R.drawable.bg_chip_success;
            case "Assigné":
                return R.drawable.bg_chip_info;
            case "En trajet":
                return R.drawable.bg_chip_primary;
            case "Indisponible":
                return R.drawable.bg_chip_warning;
            case "En cours":
                return R.drawable.bg_chip_danger;
            default:
                return R.drawable.bg_chip_primary;
        }
    }

    public static void applyStatusChip(TextView tv, String statut) {
        tv.setText(statut);
        tv.setBackgroundResource(statusChipBg(statut));
        tv.setTextColor(statusColor(tv.getContext(), statut));
    }
}
