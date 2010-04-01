/*
 * (c) 2000-2009 Carlos Gómez Rodríguez, todos los derechos reservados / all rights reserved.
 * Licencia en license/bsd.txt / License in license/bsd.txt
 */
package eu.irreality.age;

//Aetheria Remote Service Protocol Constants
public interface ARSPConstants
{

	public String COLORCODE_REQUEST = "colorcode_request";
	public String COLORCODE_INFO_BEGIN = "colorcode_info_begin";
	public String COLORCODE_INFO_LINE = "colorcode_info_line";
	public String COLORCODE_INFO_END = "colorcode_info_end";
	
	public String CLIENT_TYPE_REQUEST = "client_type_request";
	public String CLIENT_TYPE_REPLY = "client_type_reply";
	
	public String UNRECOGNIZED_MESSAGE = "unrecognized_message";
	public String UNRECOGNIZED_FORMAT = "unrecognized_format";
	public String SERVER_STATE = "server_state";
	
	public String CLEAR_SCREEN = "clear_screen";
	public String WRITE = "w";
	public String WRITE_TITLE = "w_title";
	public String FORCE_INPUT = "force_input";
	public String GET_INPUT = "g";
	public String GET_INPUT_RETURN = "gr";
	//public String GET_INPUT_ASYNCHRONOUS = "ga";
	public String SET_INPUT_STRING = "set_input_string";
	public String SET_MODE = "set_mode";
	public String WAIT_KEY_PRESS = "wait_key_press";
	public String KEY_PRESSED = "key_pressed";
	
	public String INSERT_ICON = "icon";
	public String USE_IMAGE = "image";
	public String ADD_FRAME = "add_frame";
	public String REMOVE_FRAMES = "remove_frames";
	
	public String PROTOCOL_VERSION_STATEMENT = "protocol_version_statement";
	public String PROTOCOL_VERSION_ACK = "protocol_version_ack";
	public String GOODBYE = "goodbye";
	public String SERVICE_LIST_REQUEST = "service_list_request";
	public String SERVICE_LIST_BEGIN = "service_list_begin";
	public String SERVICE_LIST_LINE = "service_list_line";
	public String SERVICE_LIST_END = "service_list_end";
	public String CALL_SERVICE = "call_service";
	public String GAME_LIST_BEGIN = "game_list_begin";
	public String GAME_LIST_LINE = "game_list_line";
	public String GAME_LIST_END = "game_list_end";
	public String ERRORMSG = "errormsg";
	public String UNSUPPORTED_SERVICE = "unsupported_service";
	
	public String WORLD_DIR = "world_dir";
	
	public String VISUALCONF_INIT_BEGIN = "visualconf_init_begin";
	public String VISUALCONF_INIT_LINE = "visualconf_init_line";
	public String VISUALCONF_INIT_END = "visualconf_init_end";
	
	public String FILE_LIST_BEGIN = "file_list_begin";
	public String FILE_LIST_LINE = "file_list_line";
	public String FILE_LIST_END = "file_list_end";
	public String GET_FILE = "get_file";
	public String FILE_HEADER_LINE = "file_header_line";
	public String FILE_ACCEPT = "file_accept";
	public String FILE_REJECT = "file_reject";

	public String MIDI_INIT = "midi_init";
	public String MIDI_PRELOAD = "midi_preload";
	public String MIDI_START = "midi_start";
	public String MIDI_OPEN = "midi_open";
	public String MIDI_STOP = "midi_stop";
	public String MIDI_CLOSE = "midi_close";
	public String MIDI_UNLOAD = "midi_unload";
	public String AUDIO_PRELOAD = "audio_preload";
	public String AUDIO_UNLOAD = "audio_unload";
	public String AUDIO_START = "audio_start";
	public String AUDIO_STOP = "audio_stop";
	
}