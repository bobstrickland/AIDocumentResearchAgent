package claudeagent.documentImport;

import ai.djl.*;
import ai.djl.inference.Predictor;
import ai.djl.ndarray.*;
import ai.djl.translate.*;
import ai.djl.huggingface.tokenizers.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

import org.springframework.beans.factory.annotation.Value;

public class DocumentEmbedder {


	@Value("${MODEL_DIR}")
	private String MODEL_DIR;
	
	private final Predictor<String, float[]> predictor;
	private final HuggingFaceTokenizer tokenizer;
	
	public DocumentEmbedder() throws IOException, MalformedModelException {
		Path modelPath = Paths.get(MODEL_DIR);

		tokenizer = HuggingFaceTokenizer.newInstance(modelPath.resolve("tokenizer.json"));
		Model model = Model.newInstance("bge-base", "OnnxRuntime");
		model.load(modelPath, "model.onnx");

		Translator<String, float[]> translator = new Translator<String, float[]> () {
			@Override
			public NDList processInput(TranslatorContext ctx, String input) throws Exception {
				NDManager manager     = ctx.getNDManager();
				Encoding encoding     = tokenizer.encode(input);
				long[] inputIds       = encoding.getIds();
				long[] attentionMasks = encoding.getAttentionMask();
				long[] typeIds        = encoding.getTypeIds();

				NDArray inputIdArray       = manager.create(inputIds).expandDims(0);
				NDArray attentionMaskArray = manager.create(attentionMasks).expandDims(0);
				NDArray typeIdArray        = manager.create(typeIds).expandDims(0);
				
				return new NDList(inputIdArray, attentionMaskArray, typeIdArray);
			}

			@Override
			public float[] processOutput(TranslatorContext ctx, NDList list) throws Exception {
				NDArray lastHiddenState = list.get(0);
				NDArray clsEmbedding = lastHiddenState.get(0).get(0);
				NDArray normalizedEmbedding = clsEmbedding.div(clsEmbedding.norm());
				return normalizedEmbedding.toFloatArray();
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
