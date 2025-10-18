package moe.lexedia

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricDefaultAttributeRegistry
import net.minecraft.block.Block
import net.minecraft.entity.EntityType
import net.minecraft.entity.SpawnGroup
import net.minecraft.entity.mob.MobEntity
import net.minecraft.item.Items
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.tag.TagKey
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.ActionResult
import net.minecraft.util.Identifier
import net.minecraft.util.math.Box
import org.slf4j.LoggerFactory
import java.util.UUID

object LeashableFences : ModInitializer {
    const val MOD_ID = "leashable-fences"
    private val logger = LoggerFactory.getLogger(MOD_ID)

    val leashConnectable: TagKey<Block> = TagKey.of(RegistryKeys.BLOCK, Identifier.of(MOD_ID, "leash_connectable"))

    val LEASH_KNOT_FAKE_ENTITY_TYPE: EntityType<LeashKnotFakeEntity> = Registry.register(
        Registries.ENTITY_TYPE,
        Identifier.of(MOD_ID, "leash_knot_fake"),
        EntityType.Builder.create({ type: EntityType<LeashKnotFakeEntity>, world ->
            LeashKnotFakeEntity(type, world)
        }, SpawnGroup.MISC)
            .dimensions(0.375f, 0.5f)
            .maxTrackingRange(10)
            .trackingTickInterval(Integer.MAX_VALUE)
            .build(RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(MOD_ID, "leash_knot_fake")))
    )

    private val pendingKnots = mutableMapOf<UUID, LeashKnotFakeEntity?>()

    override fun onInitialize() {
        logger.info("Leashable fences is initialised!")

        FabricDefaultAttributeRegistry.register(
            LEASH_KNOT_FAKE_ENTITY_TYPE,
            MobEntity.createMobAttributes()
        )

        UseBlockCallback.EVENT.register { player, world, hand, hitResult ->
            if (world.isClient) {
                return@register ActionResult.PASS
            }

            val stack = player.getStackInHand(hand)
            val pos = hitResult.blockPos
            val state = world.getBlockState(pos)

            if (stack.item == Items.LEAD && state.isIn(leashConnectable)) {
                val leashedEntities = world.getEntitiesByClass(MobEntity::class.java, player.boundingBox.expand(10.0)) { it.isLeashed && it.leashHolder == player }
                if (leashedEntities.isNotEmpty()) {
                    return@register ActionResult.PASS
                }

                val playerId = player.uuid
                val sourceKnot = pendingKnots[playerId]

                val knotsAtPos = world.getEntitiesByClass(LeashKnotFakeEntity::class.java, Box(pos)) { true }
                val targetKnot = knotsAtPos.firstOrNull()

                if (sourceKnot == null) {
                    if (targetKnot != null) {
                        pendingKnots[playerId] = targetKnot
                        world.playSound(null, pos, SoundEvents.ITEM_LEAD_TIED, SoundCategory.BLOCKS, .5f, 1.5f)
                    } else {
                        val newKnot = LeashKnotFakeEntity(LEASH_KNOT_FAKE_ENTITY_TYPE, world)
                        newKnot.setPosition(pos.x + 0.5, pos.y + 0.5 - 1.0 / 8.0, pos.z + 0.5)
                        newKnot.attachedBlockPos = pos
                        world.spawnEntity(newKnot)
                        pendingKnots[playerId] = newKnot
                        world.playSound(null, pos, SoundEvents.ITEM_LEAD_TIED, SoundCategory.BLOCKS, .5f, 1.5f)
                    }
                    return@register ActionResult.SUCCESS
                } else {
                    if (targetKnot != null) {
                        if (sourceKnot == targetKnot) {
                            pendingKnots.remove(playerId)
                            return@register ActionResult.SUCCESS
                        }
                        sourceKnot.attachLeash(targetKnot, true)
                        pendingKnots.remove(playerId)
                        if (!player.isCreative) stack.decrement(1)
                        world.playSound(null, pos, SoundEvents.ITEM_LEAD_TIED, SoundCategory.BLOCKS, .5f, 1.5f)
                    } else {
                        val newKnot = LeashKnotFakeEntity(LEASH_KNOT_FAKE_ENTITY_TYPE, world)
                        newKnot.setPosition(pos.x + 0.5, pos.y + 0.5 - 1.0 / 8.0, pos.z + 0.5)
                        newKnot.attachedBlockPos = pos
                        world.spawnEntity(newKnot)
                        sourceKnot.attachLeash(newKnot, true)
                        pendingKnots.remove(playerId)
                        if (!player.isCreative) stack.decrement(1)
                        world.playSound(null, pos, SoundEvents.ITEM_LEAD_TIED, SoundCategory.BLOCKS, .5f, 1.5f)
                    }
                    return@register ActionResult.SUCCESS
                }
            }

            return@register ActionResult.PASS
        }
    }
}