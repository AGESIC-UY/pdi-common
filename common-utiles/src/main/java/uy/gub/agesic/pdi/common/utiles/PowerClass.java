package uy.gub.agesic.pdi.common.utiles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings({"rawtypes", "unchecked"})
public class PowerClass {

	private final static Logger logger = LoggerFactory.getLogger(PowerClass.class);
	
	// ///////////////////////////////////////////////////////////////////////////////////////
	// Instance utilities
	// ///////////////////////////////////////////////////////////////////////////////////////

	public void invokeMethod(String name, Object... args) {
		Method[] methods = this.getClass().getMethods();
		if (methods != null) {
			for (int i = 0; i < methods.length; i++) {
				String methodName = methods[i].getName();
				if (methodName.equalsIgnoreCase(name)) {
					methods[i].setAccessible(true);

					try {
						methods[i].invoke(this, args);
					} catch (Throwable e) {
						logger.error("Ha ocurrido un error invocando el metodo: " + name, e);
					}

					return;
				}
			}
		}
	}

	public void invokeMethods(String regexp, Object... args) {
		Pattern pattern = Pattern.compile(regexp);

		Method[] methods = this.getClass().getMethods();
		if (methods != null) {
			for (int i = 0; i < methods.length; i++) {
				String methodName = methods[i].getName();
				Matcher matcher = pattern.matcher(methodName);
				if (matcher.find()) {
					methods[i].setAccessible(true);

					try {
						methods[i].invoke(this, args);
					} catch (Throwable e) {
						logger.error("Ha ocurrido un error invocando los metodos con nombre similar a: " + regexp, e);
					}
				}
			}
		}
	}

	public boolean existsProperty(String name) {
		List<Field> fields = new ArrayList<Field>();
		getAllFields(this.getClass(), fields);

		if (fields != null) {
			for (int i = 0; i < fields.size(); i++) {
				Field field = fields.get(i);

				if (field.getName().equalsIgnoreCase(name)) {
					return true;
				}
			}
		}

		return false;
	}

	public Object getProperty(String name) {
		List<Field> fields = new ArrayList<Field>();
		getAllFields(this.getClass(), fields);

		if (fields != null) {
			for (int i = 0; i < fields.size(); i++) {
				Field field = fields.get(i);

				if (field.getName().equalsIgnoreCase(name)) {
					field.setAccessible(true);
					try {
						return field.get(this);
					} catch (Throwable e) {
						logger.error("Ha ocurrido un error recuperando la propiedad con nombre: " + name, e);
						return null;
					}
				}
			}
		}

		return null;
	}

	public void setProperty(String name, Object value) {
		List<Field> fields = new ArrayList<Field>();
		getAllFields(this.getClass(), fields);

		if (fields != null) {
			for (int i = 0; i < fields.size(); i++) {
				Field field = fields.get(i);

				if (field.getName().equalsIgnoreCase(name)) {
					field.setAccessible(true);
					try {
						field.set(this, value);
					} catch (Throwable e) {
						logger.error("Ha ocurrido un error estableciendo la propiedad " + name, e);
						return;
					}
				}
			}
		}
	}

	// ///////////////////////////////////////////////////////////////////////////////////////
	// Class utilities
	// ///////////////////////////////////////////////////////////////////////////////////////

	public static void invokeMethod(Object instance, String name, Object... args) {
		Method[] methods = instance.getClass().getMethods();
		if (methods != null) {
			for (int i = 0; i < methods.length; i++) {
				String methodName = methods[i].getName();
				if (methodName.equals(name)) {
					methods[i].setAccessible(true);

					try {
						methods[i].invoke(instance, args);
					} catch (Throwable e) {
						logger.error("Ha ocurrido un error invocando el metodo " + name, e);
					}

					return;
				}
			}
		}
	}

	private static void getAllFields(Class<?> clazz, List<Field> allfields) {
		// Stop at object
		if (clazz.getName().indexOf("Object") != -1) {
			return;
		}

		// Get the fields
		Field[] fields = clazz.getDeclaredFields();
		if (fields != null) {
			for (int i = 0; i < fields.length; i++) {
				Field field = fields[i];
				field.setAccessible(true);

				allfields.add(field);
			}
		}

		// Go up recursivly
		getAllFields(clazz.getSuperclass(), allfields);
	}

	public static Object getProperty(Object instance, String property) {
		if (instance == null || property == null) {
			return null;
		}

		// If the instance is a map, then get it directly
		if (instance instanceof Map) {
			Map map = (Map)instance;
			return map.get(property);
		} else {
			List<Field> fields = new ArrayList<Field>();
			getAllFields(instance.getClass(), fields);
	
			if (fields != null) {
				for (int i = 0; i < fields.size(); i++) {
					Field field = fields.get(i);
	
					if (field.getName().equalsIgnoreCase(property)) {
						field.setAccessible(true);
						try {
							return correctDatatypes(field.getType(), field.get(instance));
						} catch (Throwable e) {
							logger.error("Ha ocurrido un error recuperando la propiedad " + property, e);
							return null;
						}
					}
				}
			}
		}
		
		return null;
	}

	public static void setProperty(Object instance, String property, Object value) {
		if (instance == null || property == null) {
			return;
		}

		if (instance instanceof Map) {
			Map map = (Map)instance;
			map.put(property, value);
		} else {
			List<Field> fields = new ArrayList<Field>();
			getAllFields(instance.getClass(), fields);
	
			if (fields != null) {
				for (int i = 0; i < fields.size(); i++) {
					Field field = fields.get(i);
	
					if (field.getName().equalsIgnoreCase(property)) {
						field.setAccessible(true);
						try {
							field.set(instance, value);
						} catch (Throwable e) {
							logger.error("Ha ocurrido un error estableciendo la propiedad " + property, e);
							return;
						}
					}
				}
			}
		}
	}

	public static Object[] getProperties(Object instance, String[] properties) {
		if (properties == null || properties.length == 0) {
			return null;
		}

		Object[] rowData = new Object[properties.length];

		for (int i = 0; i < properties.length; i++) {
			Object value = PowerClass.getProperty(instance, properties[i]);
			rowData[i] = value;
		}

		return rowData;
	}

	public static Class<?> getPropertyType(Object instance, String name) {
		Class<?> clazz = instance.getClass();

		List<Field> fields = new ArrayList<Field>();
		getAllFields(clazz, fields);

		if (fields != null) {
			for (int i = 0; i < fields.size(); i++) {
				Field field = fields.get(i);

				if (field.getName().equalsIgnoreCase(name)) {
					field.setAccessible(true);
					try {
						return field.getType();
					} catch (Throwable e) {
						logger.error("Ha ocurrido un error recuperando el tipo de la propiedad " + name, e);
						return null;
					}
				}
			}
		}

		return null;
	}

	public static List<String> getPropertyNames(Object instance) {
		List<String> result = new ArrayList<String>();

		Class<?> clazz = instance.getClass();

		List<Field> fields = new ArrayList<Field>();
		getAllFields(clazz, fields);

		if (fields != null) {
			for (int i = 0; i < fields.size(); i++) {
				Field field = fields.get(i);
				result.add(field.getName());
			}
		}

		return result;
	}

	public static List<String> getPropertyNamesAnnotatedWith(Object instance, Class<?> annotationClass) {
		List<String> result = new ArrayList<String>();

		Class<?> clazz = instance.getClass();

		List<Field> fields = new ArrayList<Field>();
		getAllFields(clazz, fields);

		if (fields != null) {
			for (int i = 0; i < fields.size(); i++) {
				Field field = fields.get(i);
				
				field.setAccessible(true);
				
				if (field.getAnnotations() != null) {
					for (Annotation annotation : field.getAnnotations()) {
						if (annotation.annotationType().equals(annotationClass)) {
							result.add(field.getName());
						}
					}
				}				
			}
		}

		return result;
	}

	private static Object correctDatatypes(Class<?> expectedType, Object data) {
		// This method corrects some datatypes, as Date vs. Timestamp
		if (data != null && expectedType.equals(java.util.Date.class)) {
			if (data instanceof java.sql.Date) {
				java.sql.Date date = (java.sql.Date)data;
				return new java.util.Date(date.getTime());
			} else if (data instanceof java.sql.Timestamp) {
				java.sql.Timestamp date = (java.sql.Timestamp)data;
				return new java.util.Date(date.getTime());
			}
		}
		
		return data;
	}

	public static Map<String,Class<?>> describeTypes(Object instance) {
		Map<String,Class<?>> map = new HashMap<String, Class<?>>();
		
		if (instance != null) {
			Class<?> clazz = instance.getClass();

			List<Field> fields = new ArrayList<Field>();
			getAllFields(clazz, fields);

			if (fields != null) {
				for (int i = 0; i < fields.size(); i++) {
					Field field = fields.get(i);
					field.setAccessible(true);
					try {
						Class<?> type = field.getType();
						map.put(field.getName(), type);
					} catch (Throwable e) {
						logger.error("Ha ocurrido un error recuperando los tipos las propiedades de la clase", e);
					}
				}
			}
		}
		
		return map;
	}
	
	public static Map<String,Object> describeValues(Object instance) {
		Map<String,Object> map = new HashMap<String, Object>();
		
		if (instance != null) {
			Class<?> clazz = instance.getClass();

			List<Field> fields = new ArrayList<Field>();
			getAllFields(clazz, fields);

			if (fields != null) {
				for (int i = 0; i < fields.size(); i++) {
					Field field = fields.get(i);
					field.setAccessible(true);
					try {
						Object value = correctDatatypes(field.getType(), field.get(instance));
						map.put(field.getName(), value);
					} catch (Throwable e) {
						logger.error("Ha ocurrido un error recuperando los valores de las propiedades de la clase", e);
					}
				}
			}
		}
		
		return map;
	}
	
	public static Annotation getAnnotation(Class<?> clazz, Class<?> annotationClass) {
		if (clazz.getAnnotations() != null) {
			for (Annotation annotation : clazz.getAnnotations()) {
				if (annotation.annotationType().equals(annotationClass)) {
					return annotation;
				}
			}
		}				
		return null;
	}	
}
