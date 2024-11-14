var imagePath = '';

rpc.exports = {
    init(stage, parameters) {
        imagePath = parameters.image;
        console.log("imagePath", imagePath);
    },
    dispose() {
        console.log("[dispose]");
    },
};

(function() {
    'use strict';

    if (Java.available) {
        Java.perform(function() {
            try {
                // Load required classes
                var File = Java.use('java.io.File');
                var FileInputStream = Java.use('java.io.FileInputStream');
                var ByteArrayOutputStream = Java.use('java.io.ByteArrayOutputStream');
                var Bitmap = Java.use('android.graphics.Bitmap');
                var BitmapFactory = Java.use('android.graphics.BitmapFactory');
                var CompressFormat = Java.use('android.graphics.Bitmap$CompressFormat');
                var Activity = Java.use('android.app.Activity');

                // Helper function to read bytes from file
                function readBytesFromFile(filePath) {
                    console.log('[+] Attempting to read file from:', filePath);
                    var file = File.$new(filePath);
                    if (!file.exists()) {
                        throw new Error('File does not exist: ' + filePath);
                    }

                    var fis = FileInputStream.$new(file);
                    var baos = ByteArrayOutputStream.$new();
                    var buffer = Java.array('byte', new Array(1024).fill(0));
                    var len;

                    try {
                        while ((len = fis.read(buffer, 0, buffer.length)) !== -1) {
                            baos.write(buffer, 0, len);
                        }
                        console.log('[+] File read successfully');
                        return baos.toByteArray();
                    } finally {
                        try {
                            fis.close();
                            baos.close();
                        } catch (error) {
                            console.log('[-] Error closing streams:', error);
                        }
                    }
                }

                // Hook both old and new camera result handlers
                try {
                    // Hook for Android 12+ (new ActivityResult API)
                    var ActivityResultCallback = Java.use('androidx.activity.result.ActivityResultCallback');
                    ActivityResultCallback.onActivityResult.overload('java.lang.Object').implementation = function(result) {
                        console.log('[+] New ActivityResult API called');
                        try {
                            var ActivityResult = Java.use('androidx.activity.result.ActivityResult');
                            var activityResult = Java.cast(result, ActivityResult);
                            var resultCode = activityResult.getResultCode();
                            var data = activityResult.getData();

                            if (resultCode === -1 && data !== null) {
                                handleImageReplacement(this, data);
                            }
                        } catch (error) {
                            console.log('[-] Error in new API handler:', error);
                        }
                        return this.onActivityResult(result);
                    };
                    console.log('[+] Successfully hooked new ActivityResult API');
                } catch (error) {
                    console.log('[*] New ActivityResult API not found, skipping...');
                }

                // Hook for older versions (traditional onActivityResult)
                try {
                    Activity.onActivityResult.overload('int', 'int', 'android.content.Intent').implementation = function(requestCode, resultCode, data) {
                        console.log('[+] Traditional onActivityResult called');
                        if (resultCode === -1 && data !== null) {
                            handleImageReplacement(this, data);
                        }
                        return this.onActivityResult(requestCode, resultCode, data);
                    };
                    console.log('[+] Successfully hooked traditional onActivityResult');
                } catch (error) {
                    console.log('[*] Traditional onActivityResult not found, skipping...');
                }

                // Common image replacement logic
                function handleImageReplacement(context, data) {
                    console.log('[+] Camera intent detected');
                    try {
                        if (imagePath === '') {
                            console.log('[-] No image path set. Please set an image path first.');
                            return;
                        }

                        console.log('[+] Attempting to read replacement image from:', imagePath);
                        var replacementBytes = readBytesFromFile(imagePath);
                        console.log('[+] Successfully read replacement image bytes');

                        var replacementBitmap = BitmapFactory.decodeByteArray(
                            replacementBytes,
                            0,
                            replacementBytes.length
                        );

                        if (replacementBitmap === null) {
                            throw new Error('Failed to decode bitmap');
                        }
                        console.log('[+] Successfully decoded bitmap');

                        // Handle thumbnail case
                        if (data.hasExtra('data')) {
                            console.log('[+] Found bitmap thumbnail data');
                            data.putExtra('data', replacementBitmap);
                            console.log('[+] Replaced thumbnail data');
                        }

                        // Handle full image URI case
                        var imageUri = data.getData();
                        if (imageUri !== null) {
                            console.log('[+] Found image URI:', imageUri.toString());
                            try {
                                var outputStream = context.getContentResolver().openOutputStream(imageUri);
                                if (outputStream !== null) {
                                    replacementBitmap.compress(CompressFormat.PNG.value, 100, outputStream);
                                    outputStream.flush();
                                    outputStream.close();
                                    console.log('[+] Successfully replaced URI image');
                                }
                            } catch (error) {
                                console.log('[-] Error writing to URI:', error);
                                console.log(error.stack);
                            }
                        }

                        // Handle output file case
                        var outputFileUri = data.getParcelableExtra('output');
                        if (outputFileUri !== null) {
                            console.log('[+] Found output URI:', outputFileUri.toString());
                            try {
                                var outputStream = context.getContentResolver().openOutputStream(outputFileUri);
                                if (outputStream !== null) {
                                    replacementBitmap.compress(CompressFormat.PNG.value, 100, outputStream);
                                    outputStream.flush();
                                    outputStream.close();
                                    console.log('[+] Successfully replaced output file');
                                }
                            } catch (error) {
                                console.log('[-] Error writing to output file:', error);
                                console.log(error.stack);
                            }
                        }

                        console.log('[+] Image injection completed');
                    } catch (error) {
                        console.log('[-] Error during image replacement:', error);
                        console.log(error.stack);
                    }
                }

                console.log('[+] Camera hooks installed successfully');
            } catch (error) {
                console.log('[-] Error setting up hooks:', error);
                console.log(error.stack);
            }
        });
    }
})();
