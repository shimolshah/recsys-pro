import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

class NonPersRecommender {

	
	
	private static HashMap<Integer, Integer> movie_id_index = new HashMap<Integer, Integer>();
	private static HashMap<Integer, Integer> movie_index_id = new HashMap<Integer, Integer>();

	private static HashMap<Integer, Integer> user_id_index = new HashMap<Integer, Integer>();
	private static HashMap<Integer, Integer> user_index_id = new HashMap<Integer, Integer>();

	private static float ratings_data[][];
	
	public static void main(String[] args) throws IOException {

		readMovieData();
		readUserData();
		readRatingData();

		//writeRatingData();
//		for(int i=0; i<5600; i++)
//		{
//			for(int j=0; j<100; j++)
//			{
//				System.out.println(i + " " + j + " " + ratings_data[i][j]);
//			}
//		}
		
		int input_id = Integer.valueOf(args[0]);
		System.out.println("Input movie id: " + input_id);
		System.out.println("Input movie index: " + movie_id_index.get(input_id));

		//int[] x_and_y = find_x_and_y(movie_id_index.get(input_id));
		//int x = find_x(movie_id_index.get(input_id));

		//float[] x_and_y_over_x = find_x_and_y_over_x(movie_id_index.get(input_id));
		
//		ArrayList simple_results = apply_simple_formula(movie_id_index.get(input_id));
//		int[] index_results = (int[])simple_results.get(0);
//		float[] value_results = (float[])simple_results.get(1);
//		int[] id_results = new int[index_results.length];

		ArrayList advanced_results = apply_advanced_formula(movie_id_index.get(input_id));
		int[] index_results = (int[])advanced_results.get(0);
		float[] value_results = (float[])advanced_results.get(1);
		int[] id_results = new int[index_results.length];

		
		for(int i = 0; i<index_results.length;i++)
		{
			id_results[i] = movie_index_id.get(index_results[i]);
		}

		System.out.println(Arrays.toString(id_results));
		System.out.println(Arrays.toString(value_results));

		//float[] simple = new float[x_and_y.length];
		
		//for(int i=0; i<x_and_y.length;i++)
		//{
		//	simple[i] = x_and_y[i]/(float)x;
		//	System.out.print(simple[i] + ",");
		//}
		
		
		// Movies array contains the movie IDs of the top 5 movies.
		int movies[] = new int[5];

		// Write the top 5 movies, one per line, to a text file.
		try {
			PrintWriter writer = new PrintWriter("pa1-result.txt", "UTF-8");

			for (int movieId : movies) {
				writer.println(movieId);
			}

			writer.close();

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	public static void readMovieData() throws NumberFormatException, IOException
	{
		//Read movie file to store indexes
		CSVReader reader1 = new CSVReader(new FileReader(
				"recsys-data-movie-titles.csv"),',','\0');
		String[] nextLine;
		
		int index = 0;
		while ((nextLine = reader1.readNext()) != null) {
			movie_id_index.put(Integer.valueOf(nextLine[0]), index);
			movie_index_id.put(index, Integer.valueOf(nextLine[0]));
			index++;
		}
		
		System.out.println(movie_id_index);
		reader1.close();
	}

	public static void readUserData() throws NumberFormatException, IOException
	{
		//Read user file to store indexes
		CSVReader reader1 = new CSVReader(new FileReader(
				"recsys-data-users.csv"),',','\0');
		String[] nextLine;
		
		int index = 0;
		while ((nextLine = reader1.readNext()) != null) {
			user_id_index.put(Integer.valueOf(nextLine[0]), index);
			user_index_id.put(index, Integer.valueOf(nextLine[0]));
			index++;
		}
		
		System.out.println(user_id_index);
		reader1.close();
	}

	public static void readRatingData() throws NumberFormatException, IOException
	{
		CSVReader reader1 = new CSVReader(new FileReader(
				"recsys-data-ratings.csv"));
		String[] nextLine = null;
		
		ratings_data = new float[user_id_index.size()][movie_id_index.size()];
		try
		{
			while ((nextLine = reader1.readNext()) != null) {
				ratings_data[user_id_index.get(Integer.valueOf(nextLine[0]))][movie_id_index.get(Integer.valueOf(nextLine[1]))] = Float.valueOf(nextLine[2]);
			}
		}
		catch(Exception e)
		{
			System.out.println(nextLine[0] + " " + nextLine[1]);
			throw e;
		}
		reader1.close();
	}
	
	private static int[] find_x_and_y(int input_index)
	{
		int[] x_and_y = new int[movie_id_index.size()];
		for(int i=0; i<user_id_index.size(); i++)
		{
			for(int j=0; j<movie_id_index.size(); j++)
			{
				//if( ratings_data[i][j] == ratings_data[i][input_index] && ratings_data[i][input_index] != 0)  
				if( ratings_data[i][j] != 0 && ratings_data[i][input_index] != 0)
					x_and_y[j]++;
			}			
		}
		
		for(int i=0;i<100; i++)
			System.out.print(x_and_y[i] + ", ");
		return x_and_y;
	}

	private static int[] find_not_x_and_y(int input_index)
	{
		int[] not_x_and_y = new int[movie_id_index.size()];
		for(int i=0; i<user_id_index.size(); i++)
		{
			for(int j=0; j<movie_id_index.size(); j++)
			{
				//if( ratings_data[i][j] == ratings_data[i][input_index] && ratings_data[i][input_index] != 0)  
				if( ratings_data[i][j] != 0 && ratings_data[i][input_index] == 0)
					not_x_and_y[j]++;
			}			
		}
		
		for(int i=0;i<100; i++)
			System.out.print(not_x_and_y[i] + ", ");
		return not_x_and_y;
	}
	
	private static int find_x(int input_index)
	{
		int x = 0;
		for(int i=0; i<user_id_index.size(); i++)
		{
			if( ratings_data[i][input_index] != 0)  
				x++;
		}
		System.out.println("");
		System.out.println(x);
		return x;
	}

	private static int find_not_x(int input_index)
	{
		int x = 0;
		for(int i=0; i<user_id_index.size(); i++)
		{
			if( ratings_data[i][input_index] == 0)  
				x++;
		}
		System.out.println("---");
		System.out.println(x);
		return x;
	}
	
	private static void writeRatingData() throws IOException
	{
		CSVWriter writer1 = new CSVWriter(new FileWriter(
				"temp.csv"));

		for(int i=0; i<user_id_index.size(); i++)
		{
			String[] tmp = new String[ratings_data.length];
			for(int j=0; j<100; j++)
			{
				tmp[j] = String.valueOf(ratings_data[i][j]);
			}
			writer1.writeNext(tmp);
		}
		writer1.close();
	}
	
	private static float[] find_x_and_y_over_x(int input_index)
	{
		int[] x_and_y = find_x_and_y(input_index);
		int x = find_x(input_index);

		float[] x_and_y_over_x = new float[x_and_y.length];
		for(int i=0; i<x_and_y.length;i++)
		{
			x_and_y_over_x[i] = x_and_y[i]/(float)x;
		}
		return x_and_y_over_x;
	}

	private static float[] find_not_x_and_y_over_not_x (int input_index)
	{
		int[] not_x_and_y = find_not_x_and_y(input_index);
		int not_x = find_not_x(input_index);

		float[] not_x_and_y_over_not_x = new float[not_x_and_y.length];
		for(int i=0; i<not_x_and_y.length;i++)
		{
			not_x_and_y_over_not_x[i] = not_x_and_y[i]/(float)not_x;
		}
		return not_x_and_y_over_not_x;
	}
	
	private static ArrayList apply_simple_formula(int input_index)
	{
		float[] x_and_y_over_x = find_x_and_y_over_x(input_index);
		
		//Record indexes
		HashMap<Float, ArrayList<Integer>> map = new HashMap<Float, ArrayList<Integer>>();
		for(int i=0; i<x_and_y_over_x.length;i++)
		{
			ArrayList<Integer> tmp = map.get(x_and_y_over_x[i]);
			if(tmp == null)
			{
				tmp = new ArrayList<Integer>();
				map.put(x_and_y_over_x[i], tmp);
			}
			tmp.add(i);
		}
		//sort
		
		System.out.println(Arrays.toString(x_and_y_over_x));
		
		Arrays.sort(x_and_y_over_x);
		System.out.println(Arrays.toString(x_and_y_over_x));
		
		int[] ret_val1 = new int[6];
		float[] ret_val2 = new float[6];

		int k =0;
		for(int j=0;j<6;j++)
		{
			ArrayList<Integer> list = map.get(x_and_y_over_x[x_and_y_over_x.length-1-j]);
			for(int val : list)
			{
				ret_val1[k] = val;
				ret_val2[k] = x_and_y_over_x[x_and_y_over_x.length-1-j];
				k++;
				if(k > 5) break;
			}
			if(k > 5) break;
		}
		
		ArrayList ret_val = new ArrayList();
		ret_val.add(ret_val1);
		ret_val.add(ret_val2);
		return ret_val;
	}
	
	
	private static ArrayList apply_advanced_formula(int input_index)
	{
		float[] x_and_y_over_x = find_x_and_y_over_x(input_index);
		float[] not_x_and_y_over_x = find_not_x_and_y_over_not_x(input_index);
		
		float[] x_and_y_over_x_over_not_x_and_y_over_x = new float[x_and_y_over_x.length];
		for (int i=0;i<x_and_y_over_x.length;i++)
		{
			x_and_y_over_x_over_not_x_and_y_over_x[i] = x_and_y_over_x[i] / not_x_and_y_over_x[i];
		}
		
		//Record indexes
		HashMap<Float, ArrayList<Integer>> map = new HashMap<Float, ArrayList<Integer>>();
		for(int i=0; i<x_and_y_over_x_over_not_x_and_y_over_x.length;i++)
		{
			ArrayList<Integer> tmp = map.get(x_and_y_over_x_over_not_x_and_y_over_x[i]);
			if(tmp == null)
			{
				tmp = new ArrayList<Integer>();
				map.put(x_and_y_over_x_over_not_x_and_y_over_x[i], tmp);
			}
			tmp.add(i);
		}
		//sort
		
		System.out.println(Arrays.toString(x_and_y_over_x_over_not_x_and_y_over_x));
		
		Arrays.sort(x_and_y_over_x_over_not_x_and_y_over_x);
		System.out.println(Arrays.toString(x_and_y_over_x_over_not_x_and_y_over_x));
		
		int[] ret_val1 = new int[6];
		float[] ret_val2 = new float[6];

		int k =0;
		for(int j=0;j<6;j++)
		{
			ArrayList<Integer> list = map.get(x_and_y_over_x_over_not_x_and_y_over_x[x_and_y_over_x_over_not_x_and_y_over_x.length-1-j]);
			for(int val : list)
			{
				ret_val1[k] = val;
				ret_val2[k] = x_and_y_over_x_over_not_x_and_y_over_x[x_and_y_over_x_over_not_x_and_y_over_x.length-1-j];
				k++;
				if(k > 5) break;
			}
			if(k > 5) break;
		}
		
		ArrayList ret_val = new ArrayList();
		ret_val.add(ret_val1);
		ret_val.add(ret_val2);
		return ret_val;
	}

}

class FloatComparator implements Comparator<Float>
{

	@Override
	public int compare(Float o1, Float o2) {
		if (o1 < o2)
		{
			return 1;
		}
		else if (o1 == o2)
		{
			return 0;
		}
		else
		{
			return -1;
		}
	}
	
}