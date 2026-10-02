import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;

const apiBaseUrl = String.fromEnvironment('API_BASE_URL',
    defaultValue: 'http://10.0.2.2:8080');

void main() => runApp(const OneDropApp());

class OneDropApp extends StatelessWidget {
  const OneDropApp({super.key});
  @override
  Widget build(BuildContext context) => MaterialApp(
        title: 'OneDrop',
        theme: ThemeData(colorSchemeSeed: Colors.red, useMaterial3: true),
        home: const HomePage(),
      );
}

class HomePage extends StatefulWidget {
  const HomePage({super.key});
  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  final name = TextEditingController();
  final mobile = TextEditingController();
  final bloodGroup = TextEditingController(text: 'O+');
  final hospital = TextEditingController();
  final units = TextEditingController(text: '1');
  String? userId;
  bool available = false;
  List<dynamic> requests = [];
  String message = '';

  Future<void> register() async {
    final response = await http.post(Uri.parse('$apiBaseUrl/api/users'),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode({
          'name': name.text,
          'mobileNumber': mobile.text,
          'bloodGroup': bloodGroup.text
        }));
    if (!mounted) return;
    if (response.statusCode == 201) {
      setState(() {
        userId = jsonDecode(response.body)['id'];
        message = 'Registered. You can now create requests.';
      });
    } else {
      setState(() => message = 'Registration failed: ${response.body}');
    }
  }

  Future<void> toggleAvailability(bool value) async {
    if (userId == null) return;
    final response = await http.patch(
        Uri.parse('$apiBaseUrl/api/users/$userId/availability'),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode({'available': value}));
    if (response.statusCode == 200) setState(() => available = value);
  }

  Future<void> createRequest() async {
    if (userId == null) {
      setState(() => message = 'Register before creating a request.');
      return;
    }
    final response = await http.post(Uri.parse('$apiBaseUrl/api/requests'),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode({
          'requesterId': userId,
          'bloodGroup': bloodGroup.text,
          'unitsRequired': int.tryParse(units.text) ?? 1,
          'hospital': hospital.text,
          'urgency': 'EMERGENCY',
          'additionalInformation': '',
          'latitude': 0.0,
          'longitude': 0.0
        }));
    if (!mounted) return;
    setState(() => message =
        response.statusCode == 201 ? 'Request created.' : 'Request failed.');
  }

  Future<void> loadRequests() async {
    final response = await http.get(Uri.parse('$apiBaseUrl/api/requests'));
    if (response.statusCode == 200) {
      setState(() => requests = jsonDecode(response.body) as List<dynamic>);
    }
  }

  @override
  Widget build(BuildContext context) => Scaffold(
        appBar: AppBar(title: const Text('OneDrop')),
        body: ListView(padding: const EdgeInsets.all(16), children: [
          TextField(controller: name, decoration: const InputDecoration(labelText: 'Name')),
          TextField(controller: mobile, decoration: const InputDecoration(labelText: 'Mobile number')),
          TextField(controller: bloodGroup, decoration: const InputDecoration(labelText: 'Blood group')),
          const SizedBox(height: 12),
          FilledButton(onPressed: register, child: const Text('Register')),
          SwitchListTile(title: const Text('Available to help'), value: available,
              onChanged: toggleAvailability),
          const Divider(),
          TextField(controller: hospital, decoration: const InputDecoration(labelText: 'Hospital/location')),
          TextField(controller: units, keyboardType: TextInputType.number,
              decoration: const InputDecoration(labelText: 'Units required')),
          FilledButton(onPressed: createRequest, child: const Text('Create emergency request')),
          OutlinedButton(onPressed: loadRequests, child: const Text('Refresh nearby requests')),
          if (message.isNotEmpty) Padding(padding: const EdgeInsets.symmetric(vertical: 8),
              child: Text(message)),
          ...requests.map((request) => Card(child: ListTile(
              title: Text('${request['bloodGroup']} - ${request['hospital']}'),
              subtitle: Text('${request['unitsRequired']} units • ${request['urgency']}'))))
        ]),
      );
}
