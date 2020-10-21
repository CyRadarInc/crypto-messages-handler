# CyInterceptor
#### Burp suite extension for modifying HTTP Messages relied on cryptography.
Automatically modifying parameters by using encoding/decoding,
encrypting/decrypting or hashing algorithms set in configuration tabs.  
Supported algorithms:
##### Encoding:
- Hex
- Base64
##### Encryption:
- AES CBC/ECB/CTR
- RSA
##### Hashing:
- MD5
- SHA1
- SHA256
- SHA384
- SHA512

# Installation
### Manual installation
1. Download the [latest release](https://github.com/CyRadarInc/cy-interceptor/releases) or manually compile from repo
    ```shell script
    git clone https://github.com/CyRadarInc/cy-interceptor.git
    cd cy-interceptor
    mvn clean compile assembly:single
    ```
2. Go to *Extender > Extensions > Add*, point to the location of downloaded/compiled jar file
3. Enjoy

# Usage

### Adding configuration tabs
- Click '...' or keyboard shortcut `Ctrl - N` to add an empty tab
- To clone an existing tab, use `Ctrl - Shift - N`
### Renaming tabs
- Double click on the tab title or press `F2'
- Press `Enter` when finished
### Modifying tab configurations
- Click on the switch in the top right corner to disable/enable the configuration tab
- You need to click **Apply** button to apply changes has been made or **Discard** button to cancel changes


*Note: Enabling/Disabling will also apply changes made in the current tab*

### Keyboard shortcuts
| Keystroke | Usages |
| --- | ------------ |
| `Ctrl - N` | Add a new tab |
| `Ctrl - Shift - N` | Clone the current tab |
| `Ctrl - W` | Close the current tab |
| `LEFT` | Navigate to previous tab |
| `RIGHT` | Navigate to next tab |
| `Ctrl - LEFT` | Move tab backwards |
| `Ctrl - RIGHT` | Move tab forwards |

