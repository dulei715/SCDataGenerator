package datasets;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.geocrowd.Distribution2DEnum;
import org.geocrowd.datasets.synthetic.Distribution2DGenerator;
import org.geocrowd.datasets.synthetic.ZipfGenerator;
import org.geocrowd.dtype.Rectangle;
import org.junit.Test;


public class ZipfGeneratorTest {

	@Test
	public void testZipfIncDistinctValues() {
		HashSet<Double> values = ZipfGenerator.zipfDecValues(10, true);
		Set sortedValues = new TreeSet(values);
		Iterator<Double> it = sortedValues.iterator();
		System.out.println("Size: " + sortedValues.size());
		while (it.hasNext()) {
			System.out.println(it.next());
		}
	}

	@Test
	public void testZipf() {
		HashSet<Double> values = ZipfGenerator.zipfIncValues(10, true);
		Set<Double> sortedValues = new TreeSet<>(values);
		Iterator<Double> iterator = sortedValues.iterator();
		System.out.println("Size: " + sortedValues.size());
		while (iterator.hasNext()) {
			System.out.println(iterator.next());
		}
	}
	@Test
	public void test1() {
		String filePath = "F:\\dataset\\test\\synthetic_dataset\\test1\\zipf.txt";
		Distribution2DGenerator generator = new Distribution2DGenerator(filePath);
		Rectangle rectangle = new Rectangle(0, 0, 10, 10);
		int size = 10000;
		generator.generate2DDataset(size, rectangle, Distribution2DEnum.ZIPFIAN_2D);

	}

}
