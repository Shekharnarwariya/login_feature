package com.hti.smpp.common;



public class numberr {
	
	public static void main(String [] arg) {
	int[] arr = { 1, 2, 0, 3, 0, 4, 0, 2 };
	moveZeroToEnd(arr);
	 for(int arr1:arr) {
		 System.out.print(arr1 +" ");
	 }
	 }

	public static void moveZeroToEnd(int[] arr) {
		int insertValue =0;
		for (int arr1 : arr) {
			if (arr1 != 0) {
				arr[insertValue++] = arr1;

			}

		}
		while (insertValue < arr.length) {
			arr[insertValue++] = 0;
		}
	}

}




//arr= {2,4,1,6,3,8}
//target =14
//Output = 3,5
