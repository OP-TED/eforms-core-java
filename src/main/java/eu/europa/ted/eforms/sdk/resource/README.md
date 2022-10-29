# SDK resource access
This package contains classes that address the "SDK resource access problem"

This package contains:
* `SdkDownloader`: A class that can be used to automatically discover and download new releases of the eForms SDK.
* `SdkResourceLoader`: A class that can be used to retrieve the correct version of any file in the eForms SDK.

## The SDK resource access "problem"
It is not a "problem" as such, because it has several solutions, one of which is implemented here.

Here is the "problem" definition: *When working with an eForms notice that was created with a specific version of the eForms SDK you need to access the resources contained in that specific version of the SDK. How does one locate and load the correct SDK resources each time?*

The solution to this problem is of course quite easy and one does not need a library to load some files from a folder. 

This package however provides:
* an implementation that allows an application to automatically discover and download newly released versions of the eForms SDK and place them in a folder where your application can process and use them.
* an implementation that, given an eForms SDK version number, will locate the correct SDK files you need and return them as an `InputStream` ready for processing.