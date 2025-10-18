package moe.lexedia

import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.decoration.LeashKnotEntity
import net.minecraft.entity.mob.MobEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Items
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.util.math.Vec3d

class LeashKnotFakeEntity(type: EntityType<out LeashKnotFakeEntity>, worldIn: World) : MobEntity(type, worldIn) {
    private val targetPosition = .375

    var attachedBlockPos: BlockPos? = null

    init {
        isAiDisabled = true
    }


    override fun damage(world: ServerWorld?, source: DamageSource?, amount: Float): Boolean {
        dismantle(source?.isSourceCreativePlayer != true)
        return true
    }

    override fun tick() {
        super.tick()

        val decimal = pos.y - pos.y.toInt()
        if (decimal != targetPosition) {
            val difference = targetPosition - decimal
            setPosition(pos.x, pos.y + difference, pos.z)
        }

        val blockPos = attachedBlockPos ?: BlockPos.ofFloored(pos)
        val state = world.getBlockState(blockPos)

        if (!state.isIn(LeashableFences.leashConnectable)) {
            dismantle(true)
            return
        }

        val holder = leashHolder
        if (holder != null && !holder.isAlive) {
            dismantle(true)
        }
    }


    private fun dismantle(shouldDrop: Boolean) {
        val serverWorld = world as? ServerWorld ?: return
        val soundPos = BlockPos.ofFloored(pos)
        world.playSound(null, soundPos, SoundEvents.ITEM_LEAD_BREAK, SoundCategory.BLOCKS, .5f, 1.8f)

        if (isAlive && leashHolder != null && shouldDrop) {
            dropItem(serverWorld, Items.LEAD)
        }

        remove(Entity.RemovalReason.KILLED)

        if (leashHolder is LeashKnotFakeEntity) {
            leashHolder?.remove(Entity.RemovalReason.DISCARDED)
        }
    }
}