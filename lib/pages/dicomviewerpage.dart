import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:file_picker/file_picker.dart';

class DicomViewer extends StatefulWidget {
  const DicomViewer({super.key});

  @override
  State<DicomViewer> createState() => _DicomViewerState();
}

class _DicomViewerState extends State<DicomViewer> {
  static const dicomFileChannel = MethodChannel("channelDicomFile");
  late var _imageBytes = Uint8List(0);
  late String _paitentName = "";
  bool _isImageProcessed = false;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text("Diagnostic"),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.spaceEvenly,
          crossAxisAlignment: CrossAxisAlignment.center,
          children: <Widget>[
            const Text("Welcome to Diagnoist Viewing App",
                style:
                    TextStyle(color: Colors.blue, fontWeight: FontWeight.bold)),
            Visibility(
              visible: _isImageProcessed,
              child: Column(
                children: [
                  ListTile(
                    leading: const Icon(Icons.person),
                    contentPadding: EdgeInsets.fromLTRB(50, 0, 0, 0),
                    title: Text("Name: " + _paitentName),
                  ),
                  Container(
                    transformAlignment: Alignment.center,
                    child: Padding(
                        padding: const EdgeInsets.fromLTRB(20, 20, 20, 20),
                        child: Image.memory(_imageBytes)),
                  ),
                ],
              ),
            ),
            ElevatedButton(
                style: ElevatedButton.styleFrom(
                  primary: Colors.blue[300],
                  padding: EdgeInsets.fromLTRB(20, 0, 20, 0),
                  textStyle: const TextStyle(fontSize: 20),
                ),
                onPressed: () async {
                  FilePickerResult? result = await FilePicker.platform
                      .pickFiles(
                          withData: true,
                          type: FileType.custom,
                          allowedExtensions: ['dcm'],
                          allowMultiple: true);
                  if (result != null) {
                    // implement code to process the file
                  } else {
                    const Text("Some Error Occured, Please try again!");
                  }
                },
                child: const Text("Upload Dicom File")),
          ],
        ),
      ),
    );
  }
}
