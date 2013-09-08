A multipurpose book-based suite
Current features (3.1.2):
	- right click a "press" (crafting table with inverted stairs above) with a written book to duplicate it.
	- right click an "eraser" (sponge) with a written book to unsign it.
	- /book args[]
		invalid command: Displays help/usage based on player permissions
		addpage <number> (page text): adds a page to book and quill in hand
		delpage <number>: deletes page from book and quill in hand
		f(ile)|u(rl)|l(oad) <filename|url>: Makes a book ingame. Consumes supplies.
		e(xport)|s(ave) <filename>: saves book to file, /plugins/BookSuite/SavedBooks/<filename>.book
		u(nsign): Unsigns book in hand
		a(uthor) <newAuthor>: Sets author of book in hand to specified name
		t(itle) <newTitle>: Sets title of book in hand
		l(ist): lists all files in /plugins/BookSuite/SavedBooks/
		d(elete) <filename>: delete file from /SavedBooks/ (autocompletes .book if no filetype specified)
		reload: reloads the plugin
		update: checks DevBukkit for plugin updates
		
Defaults:
	default user:
		- copy own books/book and quills for 1 book and 1 ink sack or 3 paper and 1 ink sack
		- copy maps for 9 paper
		- unsign own books using cauldron
		- send and receive mail with attachments
		- use commands addpage, delpage, list, import*, and export
			*Books imported from URL will be by author player doing the importing. Don't want random users blamed for inappropriate messages.
	op:
		- everything
