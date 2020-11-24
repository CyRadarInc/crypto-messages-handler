# Crypto Messages Handler
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
1. Download the [latest release](https://github.com/CyRadarInc/crypto-messages-handler/releases) or manually compile from repo
    ```shell script
    git clone https://github.com/CyRadarInc/crypto-messages-handler.git
    cd crypto-messages-handler
    mvn clean install
    mvn clean compile assembly:single
    ```
2. Go to *Extender > Extensions > Add*, point to the location of downloaded/compiled jar file
3. Enjoy

# Usage

### Adding configuration tabs
- Click '...' or keyboard shortcut `Ctrl - N` to add an empty tab
- To clone an existing tab, use `Ctrl - Shift - N`
### Renaming tabs
- Double click on the tab title or press `F2`
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

# About
- UI/UX inspired by [Custom Parameter Handler](https://github.com/elespike/burp-cph)
- If you find any issues or have any improving ideas about the extension's features, UI/UX design,
feel free to create an issue, create pull requests on github; or contact me via email.
# 
**Phi Cong Nguyen**  
Penetration tester, CyRadar Inc  
phinc27@cyradar.com | phinc27@gmail.com