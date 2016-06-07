package text;

import java.util.Scanner;

public class Test {
	public static void main(String[] msgn){
		Scanner sc=new Scanner(System.in);
		String str="begin";
		while(!str.equals("over")){
			str=sc.nextLine();
			System.out.println(str);	
		}
	}
}
