# TodoList-CI4-Restful

Aplikasi TodoList Android menggunakkan CodeIgniter4 berbasis Restful-API.

Proses **BackEnd** dari **Restful-API** menggunakan _**CodeIgniter4**_ pada link berikut : <a href='https://github.com/sabo99/codeigniter4_RestfulAPI_TodoList'>`https://github.com/sabo99/codeigniter4_RestfulAPI_TodoList` </a>

### **Fitur Aplikasi TodoList** :

- **_CodeIgniter4_**
  - `Upload Image (MultiPart)`
  - `Restful API (POST | GET | PUT | DELETE)`
- **API JavaMail** (_Gmail Verification Code_)
  - `Log In with 2nd Auth`
  - `Edit User`
- _**SweetAlertDialog**_
  - `Modal Dialog with Custom Content View`
  - `Message Dialog`
  - `Loading Dialog`
- **Lottie Animation**
  - `Response Code : 404 | 500`
    <br>

## Important!

### Config JavaMail APIs

In the file <a href="app/src/main/java/com/sabo/todolist_ci4_restful/Helper/JavaMailAPI/Credentials.kt">`Credentials.kt`</a> change the following line with the email that will be used as Sender

```kotlin
object Credentials {
    const val EMAIL_SENDER = "your_email"
    const val PASSWORD_SENDER = "your_password"
}
```

### Change API BASE URL

Change the API BASE URL in the following file <a href="app/src/main/java/com/sabo/todolist_ci4_restful/Restful_API/RestfulAPIService.kt">`RestfulAPIService.kt` <a/>

```kotlin
class RestfulAPIService {
    companion object {
        private const val URL = "http://192.168.1.6/Restful-API/todolist-ci4-restful/public/"
    }
}
```

### Dependencies used

```groovy
    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
    implementation 'com.github.thomper:sweet-alert-dialog:1.4.0'
    implementation 'de.hdodenhof:circleimageview:3.1.0'
    implementation 'com.squareup.picasso:picasso:2.71828'
    implementation 'com.facebook.shimmer:shimmer:0.5.0'
    implementation "com.airbnb.android:lottie:3.7.1"
    implementation 'org.greenrobot:eventbus:3.2.0'
    implementation 'androidx.swiperefreshlayout:swiperefreshlayout:1.1.0'
    api 'com.theartofdev.edmodo:android-image-cropper:2.8.+'

    implementation 'com.sun.mail:android-mail:1.6.0'
    implementation 'com.sun.mail:android-activation:1.6.0'
```

### Integration Step Used Binding in Kotlin

1. Add **viewBinding = true** <a href="app/build.gradle">`build.gralde (Module)`</a>

```groovy
android {

   buildFeatures {
      viewBinding = true
   }
}
```

2. Activity Kotlin Class

```kotlin
class MainActivity : AppCompatActivity() {

    /** Add this */
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /** Add this */
        binding = ActivityMainBinding.inflate(layoutInflater)
        /** Change this */
        setContentView(binding.root)

        /** Without findViewById */
        binding.tvMsg.text = "Bye bye findViewById"
    }
}
```

3. Activity Java Class

```java
public class MainActivity extends AppCompatActivity {

    /** Add this */
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /** Add this */
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        /** Change this */
        setContentView(binding.getRoot());

        /** Without findViewById */
        binding.textView.setText("Bye bye findViewById");
    }
}
```

**binding** in kotlin can be used directly without initializing **findViewById** on widgets in layout xml

<br><br>

## Design UI App TodoList

### Login (Activity)

<img src="ScreenShot_App/LogIn.jpg" width="250" height="450"/>

#

### Forgot Password (Modal / SweetAlertDialog)

<img src="ScreenShot_App/Forgot-Password_1.jpg" width="250" height="450"/>

Setelah meng-input email address yang telah terdaftar di database pada form email (Forgot Password), sistem akan mengirimkan kode verifikasi melalui email tersebut dengan tampilan UI seperti dibawah ini

<img src="ScreenShot_App/Modal_Send-Verification-Code_Forgot-Password.jpg" width="250" height="450"/> <img src="ScreenShot_App/Modal_Success-Send-Verification-Code_Forgot-Password.jpg" width="250" height="450"/>

Kemudian user (pengguna) diminta untuk menginputkan password baru beserta kode verifikasi yang telah dikirimkan melalui email.

<img src="ScreenShot_App/Forgot-Password_2.jpg" width="250" height="450"/>

#

### SignUp (Activity)

<img src="ScreenShot_App/SignUp.jpg" width="250" height="450"/>

#

### Main / Home (Activity)

<img src="ScreenShot_App/Home-Shimmer.jpg" width="250" height="450"/> <img src="ScreenShot_App/Home-EmptyState.jpg" width="250" height="450"/>
<img src="ScreenShot_App/Home.jpg" width="250" height="450"/> <img src="ScreenShot_App/Home-500.jpg" width="250" height="450"/>

#

### Create Todo (Activity with Modal / SweetAlertDialog)

<img src="ScreenShot_App/Create-Todo.jpg" width="250" height="450"/>

#

### Detail Todo (Activity)

<img src="ScreenShot_App/Detail-Todo.jpg" width="250" height="450"/>

#

### Edit Todo (Activity with Modal / SweetAlertDialog)

<img src="ScreenShot_App/Edit-Todo.jpg" width="250" height="450"/>

#

### Delete Todo (Modal / SweetAlertDialog)

<img src="ScreenShot_App/Modal_Delete-Todo.jpg" width="250" height="450"/>

#

### Profile (Activity)

<img src="ScreenShot_App/Profile_1.jpg" width="250" height="450"/> <img src="ScreenShot_App/Profile_2.jpg" width="250" height="450"/>

#

### Edit Profile (Activity with Card)

<img src="ScreenShot_App/Edit-Profile.jpg" width="250" height="450"/>

#

### Edit Username (Modal / SweetAlertDialog)

<img src="ScreenShot_App/Modal_Edit-Username.jpg" width="250" height="450"/>

#

### Edit Email (Modal / SweetAlertDialog)

<img src="ScreenShot_App/Modal_Edit-Email.jpg" width="250" height="450"/>

Setelah menginput "new email address" dan "current password", sistem akan mengirimkan kode verifikasi melalui email tersebut dengan tampilan UI seperti dibawah ini

<img src="ScreenShot_App/Modal_Send-Verification-Code_Change-Email.jpg" width="250" height="450"/> <img src="ScreenShot_App/Modal_Success-Send-Verification-Code_Change-Email.jpg" width="250" height="450"/>

Kemudian user (pengguna) diminta untuk melakukan validasi terhadap kode verifikasi yang telah dikirimkan melalui email.

<img src="ScreenShot_App/Modal_Verify-Change-Email.jpg" width="250" height="450"/>

#

### Edit Password (Modal / SweetAlertDialog)

<img src="ScreenShot_App/Modal_Edit-Password.jpg" width="250" height="450"/>

#

### Two Factor Authentication (Modal / SweetAlertDialog)

<img src="ScreenShot_App/Modal_Two-Factor-Auth.jpg" width="250" height="450"/>

Setelah menginput "current password", sistem akan mengirimkan kode verifikasi melalui email tersebut dengan tampilan UI seperti dibawah ini

<img src="ScreenShot_App/Modal_Send-Verification-Code_Two-Factor-Auth.jpg" width="250" height="450"/> <img src="ScreenShot_App/Modal_Success-Send-Verification-Code_Two-Factor-Auth.jpg" width="250" height="450"/>

Kemudian user (pengguna) diminta untuk melakukan validasi terhadap kode verifikasi yang telah dikirimkan melalui email.

<img src="ScreenShot_App/Modal_Verify-Two-Factor-Auth.jpg" width="250" height="450"/>

#

### Delete Account (Modal / SweetAlertDialog)

<img src="ScreenShot_App/Modal_Delete-Account.jpg" width="250" height="450"/>

#

### Log Out (Modal / SweetAlertDialog)

<img src="ScreenShot_App/Modal_LogOut.jpg" width="250" height="450"/>
