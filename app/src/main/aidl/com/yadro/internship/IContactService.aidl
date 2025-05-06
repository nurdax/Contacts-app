package com.yadro.internship;

import com.yadro.internship.IOperationCallback;

interface IContactService {
    void deleteDuplicateContacts(IOperationCallback callback);
}