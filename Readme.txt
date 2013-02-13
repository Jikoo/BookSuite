A multipurpose book-based suite
Current features (3.0.0):
	- right click a "press" (crafting table with inverted stairs above) with a written book to duplicate it.
	- right click an "eraser" (sponge) with a written book to unsign it.
	- mail a "package" by right clicking air while holding it
		Valid packages meet the following conditions.
		Title is "Package" (Capitalization does not matter)
		First page contains:
			package: <title of book message>
			to: <name of recipient>
			item: <Name of item>
		Item attachment is optional. Attached items MUST be custom named with an anvil.
		Anything written on the first page after sending data WILL be consumed. Start your message on the second.
	- unpack a "package" by right clicking while holding it
		Openable packages are titled "Package: <title of book message>"
	- /book args[]
		invalid command: Displays help/usage based on player permissions
		f(ile)|u(rl) <filename|url>: Makes a book ingame. Consumes supplies.
		e(xport)|s(ave) <filename>: saves book to file, /plugins/BookSuite/Books/<filename>.book
		e(raser): Gives a sponger, the "eraser" block. Does not conflict with export.
		u(nsign): Unsigns book in hand
		a(uthor) <newAuthor>: Sets author of book in hand to specified name
		t(itle) <newTitle>: Sets title of book in hand
		
Defaults:
	default user:
		- copy own books for 1 book and 1 ink sack
		- unsign own books using sponge
		- send and receive mail with attachments
		- use commands eraser, import*, and export
			*Books imported from URL will be by author player doing the importing. Don't want random users blamed for inappropriate messages.
	op:
		- everything
