package ru.skillbranch.skillarticles.ui.profile

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.contract.ActivityResultContract


class EditImageContract : ActivityResultContract<Pair<Uri, Uri>, Uri>(){
    override fun createIntent(context: Context, input: Pair<Uri, Uri>?): Intent {
        val intent = Intent(Intent.ACTION_EDIT).apply {
            setDataAndType(input!!.first, "image/jpeg")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            putExtra(MediaStore.EXTRA_OUTPUT, input.second)
            putExtra("return-value", true)
        }

        // scan all application on device for resolve this intent action (action Intent.ACTION_EDIT)
        val resolveInfoList = context.packageManager
            .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            .map{ info -> info.activityInfo.packageName }

        resolveInfoList.forEach {resolvePackage ->
            context.grantUriPermission(
                resolvePackage, //for package
                input!!.second, //for destination uri
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION //grant permission for write to destination Uri
            )
        }


        Log.e("EditImageContract", "activities (application) for edit image: $resolveInfoList")
        return intent
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return if(resultCode == Activity.RESULT_OK) intent?.data
        else null
    }
}