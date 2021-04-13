# Gitlet Design Document

**Vector Wang**:

## Classes and Data Structures
Similar to real git, Three classes, Blob, Commit and Tree are used to represent working files, commits and trees respectively. These three classes all inherited from BaseObject, which has a filed named id representing its unique hash. BaseObject implements Serializable. Blob has a field content representing the corresponding file content. Tree has a field named fileNameToBlobs representing the map from file name to id of Blob. Commit has a field named tree representing the Tree object the commit points to, which records all the working file history. The father and mother field of a Commit means its parent Commit. The log and timestamp field are the metadata of a Commit.
Class Index is used for stage area. Like Tree, it has a field named fileNameToBlobs recording which files are staged. Null value of an entry in fileNameToBlobs map means the corresponding file is removed and staged for removal.
Class Head is a utility class used to get the current working branch and commit info.


## Algorithms
The core function of gitlet is add, commit and checkout. For add, the working files are compared to the files in current commit, any changes(determined by file content hash) and file removal would be found and used to update the current index file. For commit, a new Tree object would be created, its fileNameToBlobs contains the fileNameToBlob of the current commit and the Index. A new Commit object would be created and points to the new Tree object. Metadata like log, timestamp and so on are recorded as well. For checkout a file, a Commit object would be constructed from the corresponding object file(identified by hash) as well as the Tree object. With fileNameToBlobs of the tree, the file content could be restored.
The other commands is based on what is demonstrated above. With the add, commit and checkout, all the important things is wrote to corresponding files. What the other commands do is just operating those files in reasonable way. 


## Persistence
All the files for gitlet is under the .gitlet directory. The HEAD is a plain text file which contains the name of current branch. The index file contains the content of stage area, which is essentially the serialized Index object. The refs directory contains the branches, where every file is a branch, the file name is same to the branch name and the file content is the head commit hash of that branch. The objects directory contains all the three important object, namely the Blob, Tree and Commit. File name of files in the objects directory is the hash of corresponding Object, and its content is the serialized Object. 

