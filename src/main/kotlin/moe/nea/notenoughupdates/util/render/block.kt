package moe.nea.notenoughupdates.util.render

import com.mojang.blaze3d.systems.RenderSystem
import org.joml.Matrix4f
import net.minecraft.client.gl.VertexBuffer
import net.minecraft.client.render.BufferBuilder
import net.minecraft.client.render.Camera
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormat
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d

class RenderBlockContext private constructor(private val tesselator: Tessellator, private val matrixStack: MatrixStack) {
    private val buffer = tesselator.buffer
    fun color(red: Float, green: Float, blue: Float, alpha: Float) {
        RenderSystem.setShaderColor(red, green, blue, alpha)
    }

    fun block(blockPos: BlockPos) {
        matrixStack.push()
        matrixStack.translate(blockPos.x.toFloat(), blockPos.y.toFloat(), blockPos.z.toFloat())
        buildCube(matrixStack.peek().positionMatrix, buffer)
        tesselator.draw()
        matrixStack.pop()
    }

    fun tinyBlock(vec3d: Vec3d, size: Float) {
        matrixStack.push()
        matrixStack.translate(vec3d.x, vec3d.y, vec3d.z)
        matrixStack.scale(size, size, size)
        matrixStack.translate(-.5, -.5, -.5)
        buildCube(matrixStack.peek().positionMatrix, buffer)
        tesselator.draw()
        matrixStack.pop()
    }

    companion object {
        private fun buildCube(matrix: Matrix4f, buf: BufferBuilder) {
            buf.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION_COLOR)
            buf.fixedColor(255, 255, 255, 255)
            buf.vertex(matrix, 0.0F, 0.0F, 0.0F).next()
            buf.vertex(matrix, 0.0F, 0.0F, 1.0F).next()
            buf.vertex(matrix, 0.0F, 1.0F, 1.0F).next()
            buf.vertex(matrix, 1.0F, 1.0F, 0.0F).next()
            buf.vertex(matrix, 0.0F, 0.0F, 0.0F).next()
            buf.vertex(matrix, 0.0F, 1.0F, 0.0F).next()
            buf.vertex(matrix, 1.0F, 0.0F, 1.0F).next()
            buf.vertex(matrix, 0.0F, 0.0F, 0.0F).next()
            buf.vertex(matrix, 1.0F, 0.0F, 0.0F).next()
            buf.vertex(matrix, 1.0F, 1.0F, 0.0F).next()
            buf.vertex(matrix, 1.0F, 0.0F, 0.0F).next()
            buf.vertex(matrix, 0.0F, 0.0F, 0.0F).next()
            buf.vertex(matrix, 0.0F, 0.0F, 0.0F).next()
            buf.vertex(matrix, 0.0F, 1.0F, 1.0F).next()
            buf.vertex(matrix, 0.0F, 1.0F, 0.0F).next()
            buf.vertex(matrix, 1.0F, 0.0F, 1.0F).next()
            buf.vertex(matrix, 0.0F, 0.0F, 1.0F).next()
            buf.vertex(matrix, 0.0F, 0.0F, 0.0F).next()
            buf.vertex(matrix, 0.0F, 1.0F, 1.0F).next()
            buf.vertex(matrix, 0.0F, 0.0F, 1.0F).next()
            buf.vertex(matrix, 1.0F, 0.0F, 1.0F).next()
            buf.vertex(matrix, 1.0F, 1.0F, 1.0F).next()
            buf.vertex(matrix, 1.0F, 0.0F, 0.0F).next()
            buf.vertex(matrix, 1.0F, 1.0F, 0.0F).next()
            buf.vertex(matrix, 1.0F, 0.0F, 0.0F).next()
            buf.vertex(matrix, 1.0F, 1.0F, 1.0F).next()
            buf.vertex(matrix, 1.0F, 0.0F, 1.0F).next()
            buf.vertex(matrix, 1.0F, 1.0F, 1.0F).next()
            buf.vertex(matrix, 1.0F, 1.0F, 0.0F).next()
            buf.vertex(matrix, 0.0F, 1.0F, 0.0F).next()
            buf.vertex(matrix, 1.0F, 1.0F, 1.0F).next()
            buf.vertex(matrix, 0.0F, 1.0F, 0.0F).next()
            buf.vertex(matrix, 0.0F, 1.0F, 1.0F).next()
            buf.vertex(matrix, 1.0F, 1.0F, 1.0F).next()
            buf.vertex(matrix, 0.0F, 1.0F, 1.0F).next()
            buf.vertex(matrix, 1.0F, 0.0F, 1.0F).next()
            buf.unfixColor()
        }

        fun renderBlocks(matrices: MatrixStack, camera: Camera, block: RenderBlockContext. () -> Unit) {
            RenderSystem.disableDepthTest()
            RenderSystem.enableBlend()
            RenderSystem.defaultBlendFunc()
            RenderSystem.setShader(GameRenderer::getPositionColorProgram)

            matrices.push()
            matrices.translate(-camera.pos.x, -camera.pos.y, -camera.pos.z)

            val ctx = RenderBlockContext(Tessellator.getInstance(), matrices)
            block(ctx)

            matrices.pop()

            RenderSystem.setShaderColor(1F,1F,1F,1F)
            VertexBuffer.unbind()
            RenderSystem.enableDepthTest()
            RenderSystem.disableBlend()
        }
    }
}


