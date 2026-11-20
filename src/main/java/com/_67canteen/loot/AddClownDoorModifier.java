package com._67canteen.loot;

import com._67canteen.registry.ModItems;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;

/** Adds a clown door to village chest loot tables selected by the modifier conditions. */
public class AddClownDoorModifier extends LootModifier {
    public static final Codec<AddClownDoorModifier> CODEC = RecordCodecBuilder.create(instance ->
            codecStart(instance).apply(instance, AddClownDoorModifier::new));

    protected AddClownDoorModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        ResourceLocation lootTableId = context.getQueriedLootTableId();
        if (lootTableId != null && lootTableId.getNamespace().equals("minecraft")
                && lootTableId.getPath().startsWith("chests/village/")) {
            generatedLoot.add(new ItemStack(ModItems.CLOWN_DOOR.get()));
        }
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
