import os
import pickle

def write_stub(filepath, object):
    if not os.path.exists(os.path.dirname(filepath)):
        os.makedirs(os.path.dirname(filepath))
    
    if filepath is not None:
        with open(filepath, 'wb') as f:
            pickle.dump(object, f)

def read_stub(read, filepath):
    if read and filepath is not None and os.path.exists(filepath):
        with open(filepath, 'rb') as f:
            object = pickle.load(f)
            return object
        
    return None