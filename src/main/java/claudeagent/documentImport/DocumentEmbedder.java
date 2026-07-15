package claudeagent.documentImport;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import ai.djl.MalformedModelException;
import ai.djl.Model;
import ai.djl.huggingface.tokenizers.Encoding;
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import ai.djl.inference.Predictor;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.translate.Batchifier;
import ai.djl.translate.NoBatchifyTranslator;
import ai.djl.translate.TranslateException;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;

@Service
public class DocumentEmbedder {
	
	private final Predictor<String, float[]> predictor;
	private final HuggingFaceTokenizer tokenizer;
	
	public DocumentEmbedder(@Value("${MODEL_DIR}") String MODEL_DIR) throws IOException, MalformedModelException {
		System.out.println(MODEL_DIR);
		File onnxDirectory = new File(MODEL_DIR);
		Path modelPath = onnxDirectory.toPath();
		System.out.println("PATH IS :"+modelPath.toString());

		tokenizer = HuggingFaceTokenizer.newInstance(modelPath.resolve("tokenizer.json"));
		Model model = Model.newInstance("bge-base", "OnnxRuntime");
		model.load(modelPath, "model.onnx");

		NoBatchifyTranslator<String, float[]> translator = new NoBatchifyTranslator<String, float[]> () {

			@Override
			public NDList processInput(TranslatorContext ctx, String input) throws Exception {
				NDManager manager     = ctx.getNDManager();
				Encoding encoding     = tokenizer.encode(input);
				long[] inputIds       = encoding.getIds();
				long[] attentionMasks = encoding.getAttentionMask();
				long[] typeIds        = encoding.getTypeIds();
			    long length = inputIds.length;

			    NDArray inputIdArray       = manager.create(inputIds, new ai.djl.ndarray.types.Shape(1, length));
			    NDArray attentionMaskArray = manager.create(attentionMasks, new ai.djl.ndarray.types.Shape(1, length));
			    NDArray typeIdArray        = manager.create(typeIds, new ai.djl.ndarray.types.Shape(1, length));

			    inputIdArray.setName("input_ids");
			    attentionMaskArray.setName("attention_mask");
			    typeIdArray.setName("token_type_ids"); 

				return new NDList(inputIdArray, attentionMaskArray, typeIdArray);
			}

			@Override
			public float[] processOutput(TranslatorContext ctx, NDList list) throws Exception {
				NDArray lastHiddenState = list.get(0);
				NDArray clsEmbedding = lastHiddenState.get(0).get(0);
				
			    float[] rawVector = clsEmbedding.toFloatArray();
			    
			    // Calculate the L2 Norm manually using standard loops
			    float sumSquared = 0.0f;
			    for (float val : rawVector) {
			        sumSquared += val * val;
			    }
			    float norm = (float) Math.sqrt(sumSquared);
			    
			    if (norm > 0) {
			        for (int i = 0; i < rawVector.length; i++) {
			            rawVector[i] /= norm;
			        }
			    }
			    
			    return rawVector;
			}
			
		};
		predictor = model.newPredictor(translator);
	}
	
	public float[] embed(String text) throws TranslateException {
		return predictor.predict(text);
	}
	
	public float[] embedQuery(String query) throws TranslateException {
		return embed("Represent this sentence for searching relevant passages: "+query);
	}
	
}
