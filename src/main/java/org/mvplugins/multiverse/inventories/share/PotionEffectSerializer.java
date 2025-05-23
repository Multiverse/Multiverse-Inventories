package org.mvplugins.multiverse.inventories.share;

import org.bukkit.potion.PotionEffect;
import org.mvplugins.multiverse.inventories.util.LegacyParsers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A simple {@link SharableSerializer} usable with PotionEffect[]
 * which converts the PotionEffect[] to the string format that is used by default in Multiverse-Inventories.
 */
final class PotionEffectSerializer implements SharableSerializer<PotionEffect[]> {

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
            return resultList.toArray(new PotionEffect[0]);
        } else {
            return LegacyParsers.parsePotionEffects(obj.toString());
        }
    }

    @Override
    public Object serialize(PotionEffect[] potionEffects) {
        return Arrays.asList(potionEffects);
    }
}
