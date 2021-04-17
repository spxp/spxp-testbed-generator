# SPXP Test Profile Generator
This tool generates random SPXP profiles for different versions of the SPXP
protocol. It takes statistical properties of existing social networks into
account to generate a realistic representation of real profiles.  
This tool is intended to generate a test set of profiles which can be used as
testbed for the development of SPXP client and server implementations.  
During the specification of new protocol versions, it is also used to test the
effect of specific protocol choices and the feasibility of design decisions.


## Preparation
This tool uses random person data generated by [randomuser.me](https://randomuser.me/),
random images from [unsplash](https://unsplash.com) for posts, random quotes
from [quotesondesign.com](http://quotesondesign.com) and random tweets from
the [Cheng-Caverlee-Lee September 2009 - January 2010 Twitter Scrape](https://archive.org/details/twitter_cikm_2010)
dataset used in the paper ["You Are Where You Tweet: A Content-Based Approach
to Geo-locating Twitter Users in CIKM 2010"](http://faculty.cse.tamu.edu/caverlee/pubs/cheng10cikm.pdf).  
The original datasets used to generate the official testbed profiles on spxp.org
is available in [./dataset](./dataset).  
Profile images are not part of this dataset and must be downloaded with the
tool [DownloadProfileImages](./src/main/java/org/spxp/tools/testbedgen/prepare/DownloadProfileImages.java)
separately.  
Other tools to scrape Photo URLs from unsplash and quotes are also located in
the package [org.spxp.tools.testbedgen.prepare](./src/main/java/org/spxp/tools/testbedgen/prepare/).


## Generating profiles of SPXP version 0.1
To generate the original testbed that can be found on http://testbed.spxp.org/0.1/,
perform these steps:

1. Run the tool `org.spxp.tools.testbedgen.prepare.DownloadProfileImages`.
It will download a set of sample profile images and store them in the local
folder `./profile_images`
2. Run the tool `org.spxp.tools.testbedgen.v01.GeneratorV01`. It will create the
files in the local folder `./v0.1`.
3. Copy or move the images from `./profile_images` to `./v0.1/images`
4. Copy the pre-created city profiles from `./dataset/places-v01` to `./v0.1`

The behavior as well as the base URL can be adopted by changing some
[constants](./src/main/java/org/spxp/tools/testbedgen/v01/GeneratorV01.java#L21)
at the top of this file.


## Generating profiles of SPXP version 0.2
To generate the original testbed that can be found on http://testbed.spxp.org/0.2/,
perform these steps:

1. Run the tool `org.spxp.tools.testbedgen.prepare.DownloadProfileImages`.
It will download a set of sample profile images and store them in the local
folder `./profile_images`
2. Run the tool `org.spxp.tools.testbedgen.v02.GeneratorV02`. It will create the
files in the local folder `./v0.2`.
3. Copy or move the images from `./profile_images` to `./v0.2/images`
4. Copy the pre-created city profiles from `./dataset/places-v02` to `./v0.2`

The behavior as well as the base URL can be adopted by changing some
[constants](./src/main/java/org/spxp/tools/testbedgen/v02/GeneratorV02.java#L43)
at the top of this file.  
Generating EC keys for the curve P-256 and calculating the ECDH shared secret
between two such keys is quite computational heavy. The generator tool writes
generated keys and computed shared secrets to files in `./cache/V0.2` and tries
to reuse such material from there on the next run. This improves speed and
guarantees reproducible profile keys.  
The symmetric round keys however are NOT cached. After re-creating profiles with
this tool, clients still need to drop and re-read all group key material.
Connections between profiles however remain.


