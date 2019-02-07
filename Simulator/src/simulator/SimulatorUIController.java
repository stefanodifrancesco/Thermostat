package simulator;

import javafx.fxml.FXML;

import javafx.scene.control.Button;

import javafx.scene.control.TextField;

import java.util.HashMap;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;

import javafx.scene.control.ChoiceBox;

public class SimulatorUIController {
	@FXML
	private TextField txtTemperature;
	@FXML
	private Button btnTemperatureStart;
	@FXML
	private ChoiceBox<String> chbxTemperatureRoom;
	@FXML
	private Button btnTemperatureStop;
	@FXML
	private Button btnPresenceStart;
	@FXML
	private ChoiceBox<String> chbxPresenceRoom;
	@FXML
	private Button btnPresenceStop;
	@FXML
	private ChoiceBox<String> chbxPresence;

	private Map<String, Thread> threadTemperatureMap = new HashMap<String, Thread>();
	private Map<String, Thread> threadPresenceMap = new HashMap<String, Thread>();

	private String url = "localhost:1883";

	@FXML
	public void initialize() {
		// fill the choiceboxes
		ObservableList<String> rooms = FXCollections.observableArrayList("kitchen", "bedroom", "livingroom", "toilet");
		ObservableList<String> presence = FXCollections.observableArrayList("Yes", "No");

		chbxPresenceRoom.setItems(rooms);
		chbxTemperatureRoom.setItems(rooms);
		chbxPresence.setItems(presence);

	}

	// Event Listener on Button[#btnTemperatureStart].onAction
	@FXML
	public void clickTemperatureStartButton(ActionEvent event) {
		// get the data
		Double temperature = Double.parseDouble(txtTemperature.getText());
		String room = chbxTemperatureRoom.getValue();

		// Check if the there is already a thread
		if (threadTemperatureMap.containsKey(room)) {

			Thread thread = threadTemperatureMap.get(room);

			// interrupt the thread
			thread.interrupt();

		}

		// Build and start the new thread with the new data
		Thread newThread = new SenderThread("temperature", room, url, temperature);

		threadTemperatureMap.put(room, newThread);

		newThread.start();

	}

	// Event Listener on Button[#btnPresenceStart].onAction
	@FXML
	public void clickPresenceStartButton(ActionEvent event) {
		// get the data
		String presenceString = chbxPresence.getValue();
		String room = chbxPresenceRoom.getValue();
		Double presence;

		// Convert in double
		switch (presenceString) {

		case "Yes":
			presence = 1d;

			break;

		case "No":
			presence = 0d;

			break;

		default:
			presence = 0d;

			break;
		}

		// Check if the there is already a thread
		if (threadPresenceMap.containsKey(room)) {

			Thread thread = threadPresenceMap.get(room);

			// interrupt the thread
			thread.interrupt();

		}

		// Build and start the new thread with the new data
		Thread newThread = new SenderThread("presence", room, url, presence);

		threadPresenceMap.put(room, newThread);

		newThread.start();
	}
}
