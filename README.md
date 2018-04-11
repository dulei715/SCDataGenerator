*** This repository is a modification of the following one: ***

https://github.com/infolab-usc/SCAWG

### Changes ###
1. Added straightforward running parameters to easily generate synthetic/synthesis data. Please see the **main.Entry** for all parameters.
2. Added support to other dataset format
### Notes ###
1. Real dataset should be put in `dataset/real/{name}` folders. Please see each processor under **org.geocrowd.synthesis** for details.
2. The result is put in `dataset/{name}/task` and `dataset/{name}/worker` for synthesis data.
For synthetic data, the result is put in `dataset/{distribution}/task` and `dataset/{distribution}/worker`.
3. Please remember clean the result folder, so that the output of different runs will not mix together.
