package com.onarandombox.multiverseinventories.share.serializers;

import com.onarandombox.multiverseinventories.util.DataStrings;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A simple {@link SharableSerializer} usable with PotionEffect[]
 * which converts the PotionEffect[] to the string format that is used by default in Multiverse-Inventories.
 */
public final class PotionEffectSerializer implements SharableSerializer<PotionEffect[]> {

    @Override
    public PotionEffect[] deserialize(Object obj) {
        if (obj instanceof List) {
            List<?> list = (List) obj;
            List<PotionEffect> resultList = new ArrayList<>(list.size());
            for (Object o : list) {
                if (o instanceof PotionEffect) {
                    resultList.add((PotionEffect) o);
                }
            }
            return resultList.toArray(new PotionEffect[resultList.size()]);
        } else {
            return DataStrings.parsePotionEffects(obj.toString());
        }
    }

    @Override
    public Object serialize(PotionEffect[] potionEffects) {
        return Arrays.asList(potionEffects);
    }
}
