package android.king.signature.config;


/***
 * 每个点的控制，关心三个因素：笔的宽度，坐标,透明数值
 *
 * @since 2018/06/15
 * @author king
 */
public class ControllerPoint {
    public float x;
    public float y;

    public float width;
    public int alpha = 255;

    public ControllerPoint() {
    }

    public ControllerPoint(float x, float y) {
        this.x = x;
        this.y = y;
    }


    public void set(float x, float y, float w) {
        this.x = x;
        this.y = y;
        this.width = w;
    }


    public void set(ControllerPoint point) {
        this.x = point.x;
        this.y = point.y;
        this.width = point.width;
    }
}
