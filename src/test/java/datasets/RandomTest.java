package datasets;

import org.geocrowd.datasets.synthetic.GaussianGenerator;

/**
 * Created by jianxun on 5/23/16.
 */
public class RandomTest {
    public static void main(String[] argv) {
        for (int i = 0; i < 50; i ++) {
            System.out.println(GaussianGenerator.GenerateInt(1, 3, 0.2));
        }
    }
}
