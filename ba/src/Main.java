import MVC.*;
public class Main {
	public static void main(String[] args) {
		MainModel model = new MainModel();
		MainView view = new MainView(model);
		new MainController(model, view);
	}
}
