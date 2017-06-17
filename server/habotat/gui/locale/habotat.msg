
window.title.habotat=Habotat

window.title.adapters=IM Adapters
window.comment.adapters=Currently Installed Adapters:

window.title.select_adapter=Select Adapter File
window.title.select_plugin_file=Select Plugin File

window.title.account=Account Properties
window.comment.account_new=Account Properties
window.comment.account=Account Properties: {0}

window.title.bot=Bot Properties
window.comment.bot_new=Bot Properties
window.comment.bot=Bot Properties: {0}

window.title.bot_console=Bot Console
window.title.about=About Habotat

window.title.select_bot_type=Select Bot Type
window.comment.select_bot_type=What type of Bot would you like to create?

window.title.select_account_type=Select Account Type
window.comment.select_account_type=What type of Account would you like to add?

window.title.preferences=Preferences
window.comment.preferences=Application Preferences

window.title.server_properties=Server Properties
window.comment.server_properties=Edit these properties to customize your server.

window.title.setup=Habotat Setup

window.title.select_dir=Select Directory

window.title.exception=Error

menu.file=File
menu.edit=Edit

menu.item.open_console=Open Console
menu.item.about=About Habotat
menu.item.exit=Exit
menu.item.im_adapters=IM Adapters...
menu.item.server_props=Server Properties...
menu.item.prefs=Preferences...

label.name=Name:
label.type=Type:
label.keyword=Keyword:
label.description=Description:
label.account=Account:
label.max_sessions=Max Sessions:
label.access_list=Access List:
label.protocol=Protocol:
label.screen_name=Screen Name:
label.password=Password:
label.comments=Comments:
label.jar_files=Jar Files
label.server_name=Server Name:
label.website=Website:
label.session_timeout=Session Timeout:
label.minutes=minutes
label.texture=Background texture:

button.auto_start_bot=Automatically start this Bot at sign-on
button.show_console=Open console at startup
button.show_splash=Show splash screen at startup
button.native_plaf=Use native Look & Feel
button.choose=Choose

heading.bot_details=Bot Details
heading.settings=Settings
heading.control_panel=Control Panel
heading.system=System
heading.accounts=Accounts
heading.plugins=Plugins
heading.bots=Bots
heading.welcome=Welcome!
heading.data_dir=Data Directory
heading.customization_opts=Customization Options
heading.adapter_config=Adapter Configuration
heading.account_details=Account Details

comment.settings_take_effect=The following settings take effect at application startup time.
comment.wizard_welcome=Welcome to Habotat! This wizard will guide you through the initial setup of your Bot server.
comment.specify_data_dir=Please specify the directory where the Habotat server and individual Bots will store their data.
comment.customize=You may customize the appearance and behavior of your Habotat server by adjusting the following options.

tooltip.add_adapter=Add Adapter
tooltip.remove_adapter=Remove Adapter
tooltip.edit_properties=Edit Properties
tooltip.sign_on=Sign On
tooltip.sign_off=Sign Off
tooltip.new_account=New Account
tooltip.edit_account=Edit Account
tooltip.remove_account=Remove Account
tooltip.start_bot=Start Bot
tooltip.stop_bot=Stop Bot
tooltip.new_bot=Create Bot
tooltip.delete_bot=Remove Bot
tooltip.edit_bot=Edit Bot
tooltip.add_plugin=Add Plugin
tooltip.remove_plugin=Remove Plugin
tooltip.reload_plugin=Reload Plugin

error.config_load_failed=Unable to read configuration file\n{0}:\n{1}
error.config_save_failed=Unable to save configuration:\n{0}
error.usage=Usage: [-hvN]
error.bot_creation=An unexpected error occurred while creating the Bot:
error.plugin_reload=An unexpected error occurredw hile reloading the Plugin:

message.nothing_selected=Please make a selection.

message.exit_confirm=All bots will be terminated and signed off. Are you sure you want to exit?

message.welcome=Welcome to Habotat!
message.prefs_updated=Preferences updated.
message.server_props_updated=Server properties updated.
message.console_initialized=System console initialized.

message.jar_not_bot=The file "{0}" does not contain a Bot Plugin.
message.plugin_referenced=The Plugin "{0}" cannot be removed; it is currently referenced by one or more Bots.
message.remove_plugin_confirm=Remove the Plugin "{0}"?
message.reload_plugin_confirm=Reload the Plugin "{0}"?

message.plugin_active=The Plugin "{0}" cannot be reloaded now because it is currently referenced by one or more active Bots. Stop those Bots first, then reload the Plugin.

message.account.required_fields=A screen name and password are required.
message.account.duplicate_name=The account "{0}" is already defined.

message.bot.required_fields=The name, keyword, type, and account fields are required.
message.bot.invalid_keyword=The keyword must start with a letter and may contain only letters, numbers, hyphens, and underscores.
message.bot.duplicate_name=The Bot "{0}" is already defined. Please choose a different name.
message.bot.invalid_name=The name must start with a letter and may contain only letters, numbers, hyphens, and underscores.

message.adapter_referenced=The adapter "{0}" cannot be removed; it is currently referenced by one or more Accounts.
message.jar_not_adapter=The file "{0}" does not contain an IM adapter.

message.stopping_bot=Stopping bot {0}...
message.starting_bot=Starting bot {0}...
message.starting_bot_failed=Unable to start Bot:\n{0}
message.delete_bot_confirm=Permanently remove the Bot "{0}"?
message.delete_bot_running=The Bot "{0}" cannot be deleted because it is currently active. Stop the Bot first.

message.starting_bots=Starting Bot(s) on {0}...
message.starting_bots_failed=Unable to start Bot(s):\n{0}
message.stopping_bots=Stopping Bot(s) on {0}...
message.account_referenced=The account "{0}" cannot be removed; it is currently referenced by one or more Bots.
message.account_delete_confirm=Permanently delete "{0}"?
message.done=Done.
message.splash=Habotat v0.5 - Mark Lindner - 16-Sep-2005

message.url_invalid=The URL is invalid.

message.file_not_dir=The file exists, but is not a directory. Please enter a different path.
message.dir_access=The directory exists, but is not readable and/or writable. Please enter a different path.
message.dir_create_confirm=The directory does not exist. Would you like to create it?
message.dir_create_failed=The directory could not be created.

column.status=
column.screen_name=Screen Name
column.protocol=Protocol
column.name=Name
column.type=Type
column.account=Account
