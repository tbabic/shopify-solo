package org.bytepoet.shopifysolo.manager.models;

import static org.hamcrest.CoreMatchers.equalTo;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class CsvBuilderTest {

	private static class TestData {
		
		private String field1;
		private Long field2;
	}
	
	@Test
	public void testCsvBuilder_Correct() {
		TestData testData1 = new TestData();
		testData1.field1 = "this is string";
		testData1.field2 = 111L;
		
		TestData testData2 = new TestData();
		testData2.field1 = "more string";
		testData2.field2 = 33L;
				
		CsvBuilder<TestData> builder = new CsvBuilder<TestData>()
				.setEncoding("windows-1250")
				.setDataObjects(Arrays.asList(testData1, testData2))
				
				.addHeaderAndField("first", td -> td.field1)
				.addHeaderAndField("2nd", td -> td.field2.toString());
		
		byte[] bytes = builder.build();
		
		String actualCsv = new String(bytes);
		String expectedCsv = "first;2nd\r\nthis is string;111\r\nmore string;33";
		
		Assert.assertThat(actualCsv, equalTo(expectedCsv));
				
				
	}
	
}
