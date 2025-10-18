package moe.lexedia.render

import moe.lexedia.LeashKnotFakeEntity
import net.minecraft.client.render.*
import net.minecraft.client.render.entity.EntityRenderer
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.render.entity.MobEntityRenderer
import net.minecraft.client.render.entity.model.EntityModelLayers
import net.minecraft.client.render.entity.model.LeashKnotEntityModel
import net.minecraft.client.render.entity.state.EntityRenderState
import net.minecraft.client.render.entity.state.LivingEntityRenderState
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
import net.minecraft.util.math.MathHelper
import org.joml.Matrix4f

class LeashKnotFakeRenderer(context: EntityRendererFactory.Context) :
    MobEntityRenderer<LeashKnotFakeEntity, LivingEntityRenderState, LeashKnotEntityModel>(
        context,
        LeashKnotEntityModel(context.getPart(EntityModelLayers.LEASH_KNOT)),
        0.0f
    ) {

    private val texture = Identifier.ofVanilla("textures/entity/lead_knot.png")

    override fun getTexture(state: LivingEntityRenderState): Identifier {
        return texture
    }

    override fun createRenderState(): LivingEntityRenderState {
        return LeashKnotFakeRenderState()
    }

    override fun shouldRender(
        entity: LeashKnotFakeEntity,
        frustum: Frustum,
        x: Double,
        y: Double,
        z: Double
    ): Boolean {
        if (entity.isLeashed) {
            return frustum.isVisible(entity.leashHolder?.boundingBox)
        } else if (super.shouldRender(entity, frustum, x, y, z)) {
            return true
        }

        return false
    }

    override fun render(
        state: LivingEntityRenderState,
        matrixStack: MatrixStack,
        vertexConsumerProvider: VertexConsumerProvider,
        light: Int
    ) {
        if (state.leashDatas != null) {
            for (leashData in state.leashDatas) {
                renderLeash(matrixStack, vertexConsumerProvider, leashData)
            }
        }

        matrixStack.push()
        matrixStack.scale(-1f, -1f, 1f)
        matrixStack.translate(0.0, 1 / -32.0, 0.0)

        val vertexConsumer = vertexConsumerProvider.getBuffer(model.getLayer(texture))
        model.render(matrixStack, vertexConsumer, light, net.minecraft.client.render.OverlayTexture.DEFAULT_UV)
        matrixStack.pop()
    }

    private fun renderLeash(
        matrices: MatrixStack,
        vertexConsumers: VertexConsumerProvider,
        leashData: EntityRenderState.LeashData
    ) {
        val f = (leashData.endPos.x - leashData.startPos.x).toFloat()
        val g = (leashData.endPos.y - leashData.startPos.y).toFloat()
        val h = (leashData.endPos.z - leashData.startPos.z).toFloat()
        val i = MathHelper.inverseSqrt(f * f + h * h) * 0.05f / 2.0f
        val j = h * i
        val k = f * i
        matrices.push()
        matrices.translate(leashData.offset)
        val vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getLeash())
        val matrix4f = matrices.peek().positionMatrix

        for (l in 0..24) {
            renderLeashSegment(vertexConsumer, matrix4f, f, g, h, 0.05f, 0.05f, j, k, l, false, leashData)
        }

        for (l in 24 downTo 0) {
            renderLeashSegment(vertexConsumer, matrix4f, f, g, h, 0.05f, 0.0f, j, k, l, true, leashData)
        }

        matrices.pop()
    }

    private fun renderLeashSegment(
        vertexConsumer: VertexConsumer,
        matrix: Matrix4f?,
        leashedEntityX: Float,
        leashedEntityY: Float,
        leashedEntityZ: Float,
        f: Float,
        g: Float,
        h: Float,
        i: Float,
        segment: Int,
        bl: Boolean,
        leashData: EntityRenderState.LeashData
    ) {
        val j = segment / 24.0f
        val k = MathHelper.lerp(j, leashData.leashedEntityBlockLight, leashData.leashHolderBlockLight)
        val l = MathHelper.lerp(j, leashData.leashedEntitySkyLight, leashData.leashHolderSkyLight)
        val m = LightmapTextureManager.pack(k, l)
        val n = if (segment % 2 == (if (bl) 1 else 0)) 0.7f else 1.0f
        val o = 0.5f * n
        val p = 0.4f * n
        val q = 0.3f * n
        val r = leashedEntityX * j
        val s = if (leashData.field_60161) {
            if (leashedEntityY > 0.0f) leashedEntityY * j * j else leashedEntityY - leashedEntityY * (1.0f - j) * (1.0f - j)
        } else {
            leashedEntityY * j
        }

        val t = leashedEntityZ * j
        vertexConsumer.vertex(matrix, r - h, s + g, t + i).color(o, p, q, 1.0f).light(m)
        vertexConsumer.vertex(matrix, r + h, s + f - g, t - i).color(o, p, q, 1.0f).light(m)
    }
}