package particles;

/**
 * Class representing a texture atlases for particles.
 */
public class ParticleTexture {
    private final int textureId;
    private final int numberOfRows;

    public ParticleTexture(int textureId, int numberOfRows) {
        this.textureId = textureId;
        this.numberOfRows = numberOfRows;
    }

    public int getTextureId() {
        return textureId;
    }

    public int getNumberOfRows() {
        return numberOfRows;
    }
}
