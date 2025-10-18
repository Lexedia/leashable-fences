package moe.lexedia.render

import net.minecraft.client.render.entity.state.LivingEntityRenderState
import net.minecraft.entity.Entity

class LeashKnotFakeRenderState : LivingEntityRenderState() {
    var leashHolder: Entity? = null
    var hasLeash = false

    var holderX = 0.0
    var holderY = 0.0
    var holderZ = 0.0

    var holderYaw = 0.0f
    var holderPitch = 0.0f
    var holderEyeHeight = 0.0f

    var isHolderHanging = false
}
