package com.cca.ia.rag.ingestor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IngestorServiceVectorDB implements IngestorService {
    private static final Logger logger = LoggerFactory.getLogger(IngestorServiceVectorDB.class);

    @Value("classpath:/data/medicaid-wa-faqs.pdf")
    private Resource pdfResource;

    @Autowired
    private VectorStore vectorStore;


    @Autowired
    private EmbeddingService embeddingService;


    @Override
    public void load() {
        PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(
                this.pdfResource,
                PdfDocumentReaderConfig.builder()
                        .withPageExtractedTextFormatter(ExtractedTextFormatter.builder()
                                .withNumberOfBottomTextLinesToDelete(3)
                                .withNumberOfTopPagesToSkipBeforeDelete(1)
                                .build())
                        .withPagesPerDocument(1)
                        .build());

        var tokenTextSplitter = new TokenTextSplitter(2000, 300, 5, 10000, true);

        List<Document> documents = tokenTextSplitter.apply(pdfReader.get());

        embeddingService.add(this.pdfResource.getFilename(), documents);

        logger.info("Parsing document, splitting, creating embeddings and storing in vector store...  this will take a while.");



        //this.vectorStore.accept(documents);
        logger.info("Done parsing document, splitting, creating embeddings and storing in vector store");

    }

    /*
    public void get() {

        FilterExpressionBuilder b = new FilterExpressionBuilder();
        Filter.Expression expression = b.eq("file_name", "medicaid-wa-faqs.pdf").build();



        SearchRequest metadataRequest = SearchRequest.defaults();
        metadataRequest.withFilterExpression(expression);

        List<Document> documents = this.vectorStore.similaritySearch(metadataRequest);

        documents.size();

    }

     */
}
