package net.mokai.quicksandrehydrated.registry;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.mokai.quicksandrehydrated.QuicksandRehydrated;

import java.util.List;

@Mod.EventBusSubscriber(modid = QuicksandRehydrated.MOD_ID)
public class ModEvents {

    @SubscribeEvent
    public static void addCustomTrades(VillagerTradesEvent e) {
        if (e.getType() == ModEntityTypes.MUDOLOGER.get()) {
            Int2ObjectMap<List<VillagerTrades.ItemListing>> trades = e.getTrades();
            int villagerLevel = 1;

            addSimpleTrade(e, 1,16, ModItems.MUSIC_DISC.get(), 1, 4);
            addSimpleTrade(e, 1, 4, Items.SLIME_BALL, 1, 16);
        }
    }

    public static void addTrade(VillagerTradesEvent e, int villagerLevel, ItemLike input, int inputCount, ItemLike product, int productCount, int retrades) {
        e.getTrades().get(villagerLevel).add((trader, rand) -> new MerchantOffer(
                new ItemStack(input, inputCount), new ItemStack(product, productCount), retrades, 8, 0.02f));
    }
    public static void addSimpleTrade(VillagerTradesEvent e, int villagerLevel, int price, ItemLike product, int productCount, int retrades) {
        e.getTrades().get(villagerLevel).add((trader, rand) -> new MerchantOffer(
                new ItemStack(Items.EMERALD, price), new ItemStack(product, productCount), retrades, 8, 0.02f));
    }

}
