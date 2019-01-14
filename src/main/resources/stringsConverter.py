import gspread
import oauth2client
import webbrowser
import http.server as BaseHTTPServer
import sys
import threading
from oauth2client.client import OAuth2WebServerFlow
import signal
import time
from oauth2client.file import Storage
import os
import sys

from lxml import etree

import argparse
import urllib.parse as urlparse

ScriptDir = os.path.dirname(__file__)

TRANSLATABLE_COLUMN = 1
DEFAULT_LANG_COLUMN = 2

class SpreadSheetManager:

	def __init__(self, productId, mode, spreadsheetKey, credentiallocation):
		print('------ Translations update script')
		print('Initializing...')
		self.HandlerClass = OauthResponseHandler
		self.ServerClass  = OauthServer
		self.Protocol     = 'HTTP/1.0'
		self.HandlerClass.protocol_version = self.Protocol
		self.port = 8888
		self.server_address = ('127.0.0.1', self.port)
		self.httpd = self.ServerClass(self.server_address, self.HandlerClass, bind_and_activate=False)
		self.httpd.allow_reuse_address = True
		self.mode = mode
		self.productId = productId

		location = ""
		if credentiallocation != '':
			location = credentiallocation + '\\'
		location = location + 'gspreadcredentials'

		self.storage = Storage(location)
		self.worksheet = None
		self.spreadsheetKey = spreadsheetKey





	def startServer(self):
		print('Starting web server')
		self.httpd.server_bind()
		self.httpd.server_activate()
		sa = self.httpd.socket.getsockname()
		self.thread = threading.Thread(None, self.httpd.handle_request())
		self.thread.start()

		return

	def stopServer(self):
		self.httpd.server_close()
		sys.exit()


	def requestOauth(self, googleAppClientId, googleAppClientSecret):
		self.credentials = self.storage.get()

		if self.credentials is None or self.credentials.invalid:
			self.flow = OAuth2WebServerFlow(client_id= googleAppClientId,
			                                client_secret= googleAppClientSecret,
			                                scope='https://spreadsheets.google.com/feeds/',
			                                redirect_uri='http://localhost:8888')
			auth_uri = self.flow.step1_get_authorize_url()
			print(auth_uri)
			webbrowser.open(auth_uri)
			self.startServer()
		else:
			self.openSheet(None)
			parser = LocalizableStringsParser(manager.mode)
			parser.perform(self.worksheet)
		return


	def openSheet(self, code):
		if self.credentials is None or self.credentials.invalid:
			self.credentials  = self.flow.step2_exchange(code)
		self.storage.put(self.credentials)
		self.gc = gspread.authorize(self.credentials)

		try:
			self.spreadsheet = self.gc.open_by_key(self.spreadsheetKey)

			if self.mode == 'apply':
				try:
					self.worksheet = self.spreadsheet.worksheet(self.productId)
					self.spreadsheet.del_worksheet(self.worksheet)
				except:
					print('sheet does not exist, cannot be deleted')
				self.worksheet = self.spreadsheet.add_worksheet(self.productId, rows='1000', cols='4')
			else :
				try:
					self.worksheet = self.spreadsheet.worksheet(self.productId)
				except gspread.WorksheetNotFound:
					sys.exit("Translation Google Worksheet not found, product id %s may be wrong." %self.productId)
		except gspread.SpreadsheetNotFound:
			sys.exit("Translation Google Spreadsheet not found, spreadsheet key %s may be wrong." %self.spreadsheetKey)
		return



class OauthResponseHandler(BaseHTTPServer.BaseHTTPRequestHandler):
	#Handler for the GET requests
	def do_GET(self):
		o = urlparse.urlparse(self.path)
		parse = urlparse.parse_qs(o.query)
		print(parse['code'][0])
		manager.openSheet(parse['code'][0])
		parser = LocalizableStringsParser(manager.mode)
		parser.perform(manager.worksheet)
		self.send_response(200)
		self.send_header('Content-type','text/html')
		self.end_headers()
		self.wfile.write(bytes('<img src="http://i3.kym-cdn.com/photos/images/newsfeed/000/296/441/24a.jpg"></img>', "utf-8"))
		self.wfile.close()
		manager.stopServer()
		return

class OauthServer(BaseHTTPServer.HTTPServer):
	allow_reuse_address = True



class LocalizableStringsParser:
	def __init__(self, mode):

		self.paths   = []
		self.languages     = LANGList

		self.paths.append(os.path.join(ScriptDir,   OUTPUTPath + "\\values\\strings.xml"))

		for lang in self.languages :
			self.paths.append(os.path.join(ScriptDir,   OUTPUTPath + "\\values-" + lang + "\\strings.xml"))

		self.translatableColumn = 1
		self.baseColumn = DEFAULT_LANG_COLUMN

		self.rows = []
		self.mode = mode


	def getIndexes(self):

		indexes = set()

		sarrayCount = 0

		for path in self.paths :

			tree = etree.parse(path)
			for element in tree.xpath("/resources/string-array"):
				id = 0
				for subElement in element.findall("item"):
					indexes.add(element.attrib['name'] + "_itemarray_" + str(id))
					id = id +1
				sarrayCount = sarrayCount + id

			for element in tree.xpath("/resources/string"):
				indexes.add(element.attrib['name'])


		print('Total array items : ' + str(sarrayCount))
		print('Total indexes : ' + str(len(indexes)))

		return indexes


	def populateRows(self):

		missing = set()
		for elementIndex in self.getIndexes():

			element  = self.findBaseLine(elementIndex)
			if element is None:

				missing.add(element)
				print('ERROR : missing element index in base : '+ elementIndex)
				continue

			print('Got element in language BASE : ' + element.text)
			translatable = ''

			row = []
			row.append(elementIndex)

			if element.get('translatable') is None or element.get('translatable') == 'true':
				langId = 0
				row.append(translatable)
				row.append(element.text)
				for lang in self.languages :
					print("For language " + lang)
					cElement  = self.findCorrespondingLanguageLine(elementIndex, self.paths[langId + 1])
					if cElement == '':
						print('missing ' + lang + 'Element for : ' + elementIndex)

					print('Got element in language ' + lang + ' : ' + cElement)
					row.append(cElement)

					langId = langId + 1

			else:
				translatable = 'false'
				row.append(translatable)
				row.append(element.text)

			self.rows.append(row)

		if len(missing) > 0:
			print('Missing ' + str(len(missing)) + ' element(s)')
			return False

		return True


	def findCorrespondingLanguageLine(self, elementId, path):
		tree = etree.parse(path)

		if "_itemarray_" not in elementId:

			for element in tree.findall("//string[@name='"+ elementId + "']"):

				if element.text is not None and len(element.text)> 0:
					return element.text
		else :
			parentElement = elementId.split('_itemarray_')[0]
			itemId = elementId.split('_itemarray_')[1]
			for element in tree.findall("//string-array[@name='"+ parentElement + "']"):
				i = 0
				for subElement in element.findall("item"):
					if str(i) == itemId:
						return subElement.text
					i = i +1

		return ''


	def populateWorksheet(self, worksheet):
		numberOfColumns = 5
		count = len(self.rows)
		if count == 0: count = 1

		rangeEnd = 'E'+str(count)
		cells = worksheet.range('A1:'+rangeEnd)
		print('Total cells entries : ' + str(len(cells)))
		for index, cell in enumerate(cells):
			rowIndex = index//numberOfColumns
			columnIndex = index%numberOfColumns
			#	print('Row : ' + str(rowIndex) + ' Column : ' + str(columnIndex))

			cell.value = self.rows[rowIndex][columnIndex]
			value = cell.value
			if value is None:
				value = ''


		worksheet.update_cells(cells)
		return


	def findBaseLine(self, elementId):
		tree = etree.parse(self.paths[0])

		if "_itemarray_" not in elementId:
			for baseElement in tree.findall("//string[@name='"+ elementId + "']"):

				if baseElement.text is not None and len(baseElement.text)> 0:
					return baseElement

		else :
			parentElement = elementId.split('_itemarray_')[0]
			print('parentelement is ' + parentElement)
			itemId = elementId.split('_itemarray_')[1]
			print('itemId is ' + itemId)
			for baseElement in tree.findall("//string-array[@name='"+ parentElement + "']"):
				i = 0
				for subElement in baseElement.findall("item"):
					if str(i) == itemId:
						return subElement
					i = i +1

		return None

	def lineData(self, line):
		lineData = line.split('=')
		sanitizedData = []
		for data in lineData:
			sanitizedData.append(self.sanitize(data))

		return sanitizedData


	def sanitize(self, line):
		if len(line) > 0:
			line = line.strip()
			line = line.replace('"', '')
			line = line.replace(';', '')
			return line
		return ''


	def apply(self, worksheet):
		print('Applying values from local')

		if self.populateRows():
			self.populateWorksheet(worksheet)
			print('Remote file updated')

		else :
			print(' ==> Update error, stop')
			sys.exit(0)
		return


	def retrieve(self, worksheet):
		print('Retrieving values from remote...');
		self.populateFiles(worksheet)
		print('Local files upated !')
		return

	def populateFiles(self, worksheet):
		rows = worksheet.get_all_values()
		print(str(len(rows))+" rows found, populating local files...")

		missing = list()

		columnIndex = 2
		for path in self.paths :

			missing.extend(self.populateFile(path,columnIndex, rows))
			columnIndex = columnIndex + 1

		print('missing elements in base:')
		print(missing)


		return

	def populateFile(self, path, index, rows):

		print('populate , path: ' + path)
		top = etree.Element('resources')

		missing = list()
		with open(path, 'wb') as doc:

			for row in rows:
				if index < len(row)  and row[index] is not None and len(row[index]) > 0:

					# is an array
					if "_itemarray_"  in row[0]:
						parentElementIndex = row[0].split('_itemarray_')[0]
						print('parentElementIndex is ' + parentElementIndex)
						itemId = row[0].split('_itemarray_')[1]
						print('itemId is ' + itemId)

						parentElement = None
						for baseElement in top.xpath("//string-array[@name='"+ parentElementIndex + "']"):
							parentElement = baseElement
							break

						if parentElement is None:
							parentElement = etree.SubElement(top,'string-array')
							parentElement.attrib['name'] = parentElementIndex

						subElement = etree.SubElement(parentElement,'item')
						subElement.text = row[index]

					else: # is not an array

						child = etree.SubElement(top,'string')
						child.text = row[index]
						child.attrib['name'] = row[0]

				if index == self.baseColumn and len(row[index]) == 0:
					missing.append(row[0])

				if index != self.baseColumn and len(row[self.baseColumn]) == 0:
					missing.append(row[0])

				if index == self.baseColumn and len(row[self.translatableColumn]) > 0:
					child.attrib['translatable'] = row[self.translatableColumn]


			doc.write(etree.tostring(top, pretty_print=True, encoding='UTF-8', xml_declaration=True))
			doc.close()

		return missing



	def perform(self, worksheet):
		#print('Performing '+mode)
		if self.mode == 'retrieve':
			self.retrieve(worksheet)
		elif self.mode == 'apply':
			self.apply(worksheet)
		return


if __name__ == '__main__':
	parser = argparse.ArgumentParser(description='Synchronize translations')
	parser.add_argument('productid', type=str, help='Product identifier referring to the worksheet name')
	parser.add_argument('-m', '--mode', type=str, choices=['apply', 'retrieve'], default='retrieve', help='Apply or retrieve translations')
	parser.add_argument('-sk', '--spreadsheetkey', type=str, default='', help='Specify a custom spreadsheet key, defaults to original mcs spreadsheet')
	parser.add_argument('-o', '--output', type=str, default='src/main/res', help='Specify a resource directory')
	parser.add_argument('-gcid', '--clientid', type=str, help='Specify a google client id to access Google sheet')
	parser.add_argument('-gcsecret', '--clientsecret', type=str, help='Specify a google client secret to access Google sheet')
	parser.add_argument('-cl', '--credentiallocation', type=str, default= "", help='Specifiy a location for google credentials')
	parser.add_argument('-l', '--lang', type=str, help='Specify a list of managed languages')
	args = parser.parse_args()

# print('languages are '  +	" ".join(args.lang))

print('resource is ' + args.output)

argLang = args.lang.replace(" ", "")

LANGList = argLang.split(",")
for s in LANGList:
	print(s)
OUTPUTPath = args.output

manager = SpreadSheetManager(args.productid, args.mode, args.spreadsheetkey, args.credentiallocation)

try:
	manager.requestOauth(args.clientid, args.clientsecret)
except KeyboardInterrupt:
	print(', shutting down the web server')
	manager.stopServer()
	sys.exit(0)









