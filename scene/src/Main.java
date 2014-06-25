import MVC.*;
public class Main {
		
	public static void main(String[] args) {
		
		String task = "sandbox";
		if(args.length > 0) {
			Integer configurationFlag = Integer.valueOf(args[0]);
			System.out.println(configurationFlag);
			switch (configurationFlag){
			case 1:
				task = "flss_map";
				break;
			case 2:
				task = "nmm_map";
				break;
			case 3:
				task = "pq_map";
				break;
			case 4:
				task = "gem_map";
				break;
			case 5:
				task = "flss_snake";
				break;
			case 6:
				task = "nmm_snake";
				break;
			case 7:
				task = "pq_snake";
				break;
			case 8:
				task = "gem_snake";
				break;
			}
		}

		MainModel model = new MainModel(task);
		MainView view = new MainView(model);
		new MainController(model, view);
	}
	
}
