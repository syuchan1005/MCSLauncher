package com.github.syuchan1005.mcslauncher

import javafx.embed.swing.SwingFXUtils
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import java.awt.geom.AffineTransform
import java.awt.image.AffineTransformOp
import java.awt.image.BufferedImage
import java.net.URL
import javax.imageio.ImageIO


/**
 * Created by syuchan on 2017/05/20.
 */
class PlayerModelCell : ListCell<Controller.PlayerModel>() {
    override fun updateItem(item: Controller.PlayerModel?, empty: Boolean) {
        if (item != null) {
            this.graphic = createNode(item)
        }
    }

    fun createNode(model: Controller.PlayerModel): Node {
        val hBox = HBox()
        val skinImage = ImageIO.read(URL("http://skins.minecraft.net/MinecraftSkins/" + model.name + ".png")).getSubimage(8, 8, 8, 8)
        val imageView = ImageView(SwingFXUtils.toFXImage(scale(skinImage, 4.0), null))
        hBox.children += imageView
        val vBox = VBox()
        vBox.children += Label(model.name)
        vBox.children += Label(model.ip)
        hBox.children += vBox
        return hBox
    }

    private fun scale(bufferedImage: BufferedImage, scale: Double): BufferedImage {
        val tx = AffineTransform()
        tx.scale(scale, scale)
        return AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR).filter(bufferedImage, null)
    }
}