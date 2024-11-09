package de.yw.depthperception

import java.awt.Color
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.sqrt

fun main() {
    val imageWidth = 128
    val imageHeight = 128

    val cameraTranslation = Vector(3)
    cameraTranslation[0] = 0f
    cameraTranslation[1] = 0f
    cameraTranslation[2] = 12f
    val extrinsicMatrix = createExtrinsicMatrix(cameraTranslation)
    val intrinsicMatrix = createIntrinsicMatrix(800f, 800f, imageWidth / 2f, imageHeight / 2f)
    val projectionMatrix = intrinsicMatrix * extrinsicMatrix

    val cube1 = Vector(4)
    cube1[0] = -0.5f
    cube1[1] = -0.5f
    cube1[2] = 0f
    cube1[3] = 1f

    val cube2 = Vector(4)
    cube2[0] = 0.5f
    cube2[1] = -0.5f
    cube2[2] = 0f
    cube2[3] = 1f

    val cube3 = Vector(4)
    cube3[0] = 0.5f
    cube3[1] = 0.5f
    cube3[2] = 0f
    cube3[3] = 1f

    val cube4 = Vector(4)
    cube4[0] = -0.5f
    cube4[1] = 0.5f
    cube4[2] = 0f
    cube4[3] = 1f

    val cube5 = Vector(4)
    cube5[0] = -0.5f
    cube5[1] = -0.5f
    cube5[2] = 5f
    cube5[3] = 1f

    val cube6 = Vector(4)
    cube6[0] = 0.5f
    cube6[1] = -0.5f
    cube6[2] = 5f
    cube6[3] = 1f

    val cube7 = Vector(4)
    cube7[0] = 0.5f
    cube7[1] = 0.5f
    cube7[2] = 5f
    cube7[3] = 1f

    val cube8 = Vector(4)
    cube8[0] = -0.5f
    cube8[1] = 0.5f
    cube8[2] = 5f
    cube8[3] = 1f


    val cube = listOf(
        cube1,
        cube2,
        cube3,
        cube4,
        cube5,
        cube6,
        cube7,
        cube8
    )

    generateCover(imageWidth, imageHeight, cube, projectionMatrix)

    for (index in 0 .. 100) {
        generateFrameWithRandomLines(imageWidth, imageHeight, cube, projectionMatrix, index)
    }
}

private fun generateCover(
    imageWidth: Int,
    imageHeight: Int,
    cube: List<Vector>,
    projectionMatrix: Matrix,
) {
    val image = BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB)

    val graphics = image.createGraphics()
    cube.forEach {
        val pixelCoordinates = it.transform(projectionMatrix).toCartesian()
        val width = 3f
        val green = 1f - it[2] / 10
        graphics.color = Color(0f, (green).coerceAtLeast(0f), 0f)
        graphics.fillRect(
            pixelCoordinates[0].toInt() - (width / 2).toInt(),
            pixelCoordinates[1].toInt() - (width / 2).toInt(),
            width.toInt(),
            width.toInt()
        )
    }

    val frontCorners = cube.map {
        it.transform(projectionMatrix).toCartesian()
    }
    graphics.drawLines(frontCorners)
    val outputFile = File("src/main/resources/cover.png")

    ImageIO.write(image, "png", outputFile)
}

private fun generateFrameWithRandomLines(
    imageWidth: Int,
    imageHeight: Int,
    cube: List<Vector>,
    projectionMatrix: Matrix,
    index: Int
) {
    val image = BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB)

    val graphics = image.createGraphics()
    cube.forEach {
        val pixelCoordinates = it.transform(projectionMatrix).toCartesian()
        val width = 3f
        val green = 1f - it[2] / 10
        graphics.color = Color(0f, (green).coerceAtLeast(0f), 0f)
        graphics.fillRect(
            pixelCoordinates[0].toInt() - (width / 2).toInt(),
            pixelCoordinates[1].toInt() - (width / 2).toInt(),
            width.toInt(),
            width.toInt()
        )
    }

    val frontCorners = cube.map {
        it.transform(projectionMatrix).toCartesian()
    }
    graphics.drawLines(frontCorners.shuffled())
    val outputFile = File("src/main/resources/frame$index.png")

    ImageIO.write(image, "png", outputFile)
}

private fun Graphics2D.drawLines(frontCorners: List<Vector>) {
    val frontEdges = mutableListOf<List<Vector>>()
    for (i in 0 until frontCorners.size) {
        val firstCorner = i % frontCorners.size
        val secondCorner = (i + 1) % frontCorners.size
        frontEdges.add(listOf(frontCorners[firstCorner], frontCorners[secondCorner]))
    }

    frontEdges.add(listOf(frontCorners.first(), frontCorners.last()))
    frontEdges.forEach {
        this.color = Color(0f, 0.5f, 0f)
        this.drawLine(it.first()[0].toInt(), it.first()[1].toInt(), it.last()[0].toInt(), it.last()[1].toInt())
    }
}

fun createIntrinsicMatrix(fx: Float, fy: Float, cx: Float, cy: Float): Matrix {
    val result = Matrix(3, 3)
    result[0, 0] = fx
    result[1, 1] = fy
    result[2, 0] = cx
    result[2, 1] = cy
    result[2, 2] = 1f
    return result
}

fun createExtrinsicMatrix(translation: Vector): Matrix {
    val result = Matrix(3, 4)

    // unity matrix for rotation
    result[0, 0] = 1f
    result[1, 1] = 1f
    result[2, 2] = 1f

    // translation matrix
    result[3, 0] = translation[0]
    result[3, 1] = translation[1]
    result[3, 2] = translation[2]

    return result
}

// https://de.wikipedia.org/wiki/Matrix_(Mathematik)#/media/Datei:Matrix_german.svg
class Matrix(val rows: Int, val columns: Int) {

    val array: Array<Array<Float>> = Array(columns) { Array(rows) { 0f } }

    operator fun get(row: Int, column: Int) = array[row][column]

    operator fun set(row: Int, column: Int, value: Float) {
        array[row][column] = value
    }

    override fun toString(): String {
        var string = ""
        for (row in 0..<rows) {
            for (column in 0..<columns) {
                string += array[column][row]
                string += " "
            }
            string += "\n"
        }
        return string
    }

    operator fun times(other: Matrix): Matrix {
        require(columns == other.rows)
        val result = Matrix(this.rows, other.columns)

        for (i in 0..<rows) {
            for (k in 0..<other.columns) {
                for (j in 0..<columns) {
                    result[k, i] += this[j, i] * other[k, j]
                }
            }
        }
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Matrix

        if (columns != other.columns) return false
        if (rows != other.rows) return false

        for (column in 0..<columns) {
            for (row in 0..<rows) {
                if (this[column, row] != other[column, row]) {
                    return false
                }
            }
        }

        return true
    }

    override fun hashCode(): Int {
        var result = columns
        result = 31 * result + rows
        result = 31 * result + array.contentDeepHashCode()
        return result
    }
}

class Vector(val dimensions: Int) {
    val array: Array<Float> = Array(dimensions) { 0f }

    fun transform(matrix: Matrix): Vector {
        require(matrix.columns == dimensions)
        val result = Vector(matrix.rows)

        for (row in 0..<matrix.rows) {
            for (column in 0..<matrix.columns) {
                val matrixValue = matrix[column, row]
                val vectorValue = this[column]
                val multiplied = matrixValue * vectorValue
                result[row] += multiplied
            }
        }
        return result
    }

    operator fun set(n: Int, value: Float) {
        array[n] = value
    }

    operator fun get(n: Int) = array[n]

    fun normalize(): Vector {
        val magnitude = magnitude()
        val result = Vector(dimensions)
        for (i in 0 until dimensions) {
            result[i] = this[i] / magnitude
        }
        return result
    }

    fun magnitude() = sqrt(array.sum())

    fun toHomogenous(): Vector {
        val result = Vector(dimensions + 1)
        val normalized = this.normalize()
        for ((index, value) in normalized.array.withIndex()) {
            result[index] = value
        }
        result[dimensions] = 1f
        return result
    }

    fun toCartesian(): Vector {
        val result = Vector(dimensions - 1)
        for (i in 0 until result.dimensions) {
            result[i] = this[i] / this[dimensions - 1]
        }
        return result
    }

    override fun toString(): String {
        var string = ""
        for (column in 0..<dimensions) {
            string += array[column]
            string += "\n"
        }
        return string
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Vector

        if (dimensions != other.dimensions) return false
        if (!array.contentEquals(other.array)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = dimensions
        result = 31 * result + array.contentHashCode()
        return result
    }
}
