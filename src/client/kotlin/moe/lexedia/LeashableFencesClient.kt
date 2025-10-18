package moe.lexedia

import moe.lexedia.render.LeashKnotFakeRenderer
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry

object LeashableFencesClient : ClientModInitializer {
	override fun onInitializeClient() {
        EntityRendererRegistry.register(LeashableFences.LEASH_KNOT_FAKE_ENTITY_TYPE, ::LeashKnotFakeRenderer)
	}
}
