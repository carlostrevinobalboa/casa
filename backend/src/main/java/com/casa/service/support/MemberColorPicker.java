package com.casa.service.support;

import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class MemberColorPicker {

    private static final String[] COLORS = {
        "#1D4ED8",
        "#B91C1C",
        "#047857",
        "#7C3AED",
        "#B45309",
        "#0F766E",
        "#BE185D",
        "#334155"
    };

    public String pick(UUID userId) {
        int index = Math.floorMod(userId.hashCode(), COLORS.length);
        return COLORS[index];
    }
}
