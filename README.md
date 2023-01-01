# Modes adapter
Control light bulbs from [WiZ](https://www.wizconnected.com/en-gb) through the local UDP interface.

## Configuration
The adapter supports sharing configuration files with other adapters by using the namespace `wiz`.
Within `lamps` each lamp is listed, identified by its device name. A lamp contains one or more `bulbs`, identified by its ip address or hostname.

### Example
```yaml
wiz:
   lamps:
    - device:
         room: 'Living room'
         name: 'Dining table'
      bulbs:
       - ip: 10.11.65.31
       - ip: 10.11.65.32
       - ip: 10.11.65.33
       - ip: 10.11.65.34
```

In this example we have created one lamp with four separate bulbs. The lamp will show up as one device and one entity in Home Assistant.
