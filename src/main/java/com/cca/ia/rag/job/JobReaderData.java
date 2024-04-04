package com.cca.ia.rag.job;

import com.cca.ia.rag.document.DocumentService;
import com.cca.ia.rag.document.parser.DocumentParser;
import com.cca.ia.rag.job.model.JobContent;
import com.cca.ia.rag.job.model.JobEntity;
import com.cca.ia.rag.s3.RemoteFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JobReaderData implements JobExecutor {

    @Autowired
    private JobRepository repository;

    @Autowired
    private DocumentParser pdfDocumentParser;

    @Autowired
    private RemoteFileService remoteFileService;

    @Autowired
    private DocumentService documentService;

    @Override
    public void execute(JobEntity job, JobContent jobContent) throws Exception {

        /*
        String objectName = jobContent.getUri();
        if (objectName.endsWith(".pdf")) {
            parsePdfDocument(job, jobContent);
            return;
        }
*/
        throw new Exception("Unsupported file type");
    }

    private void parsePdfDocument(JobEntity job, JobContent jobContent) throws Exception {

        /*
        String collectionId = jobContent.getCollectionId();
        String objectName = jobContent.getUri();

        InputStream file = remoteFileService.getObject(collectionId, objectName);
        Resource resource = new InputStreamResource(file);

        List<Document> documents = pdfDocumentParser.parse(resource);
        documentService.persistDocumentAndChunks(collectionId, objectName, documents);

         */
    }
}
